const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { getFirestore } = require("firebase-admin/firestore");
const { initializeApp } = require("firebase-admin/app");
const { GoogleGenerativeAI } = require("@google/generative-ai");
const axios = require("axios");
const pdfParse = require("pdf-parse");

initializeApp();
const db = getFirestore();

// Initialize Gemini (Requires GEMINI_API_KEY to be set in Firebase Config or Environment Variables)
// To set config in Firebase: firebase functions:config:set gemini.key="YOUR_API_KEY"
const geminiApiKey = process.env.GEMINI_API_KEY; 

exports.parseCvWithGemini = onCall(async (request) => {
    // 1. Verify Authentication
    if (!request.auth) {
        throw new HttpsError("unauthenticated", "User must be logged in to parse CV.");
    }

    const data = request.data;
    const userUid = data.userUid;
    const downloadUrl = data.downloadUrl;
    const cvId = data.cvId;

    if (!userUid || !downloadUrl || !cvId) {
        throw new HttpsError("invalid-argument", "Missing required parameters: userUid, downloadUrl, cvId.");
    }

    if (!geminiApiKey) {
        console.error("GEMINI_API_KEY is not set in the environment.");
        throw new HttpsError("internal", "Gemini API key is not configured on the server.");
    }

    try {
        console.log(`Starting CV parsing for user ${userUid}, CV ${cvId}`);

        // 2. Download the PDF
        console.log("Downloading PDF from:", downloadUrl);
        const response = await axios.get(downloadUrl, { responseType: "arraybuffer" });
        const pdfBuffer = Buffer.from(response.data);

        // 3. Extract Text using pdf-parse
        console.log("Extracting text from PDF...");
        const pdfData = await pdfParse(pdfBuffer);
        const textContent = pdfData.text;

        if (!textContent || textContent.trim() === "") {
             throw new HttpsError("invalid-argument", "Could not extract text from the provided PDF.");
        }

        console.log(`Extracted ${textContent.length} characters of text.`);

        // 4. Call Gemini API
        console.log("Calling Gemini API...");
        const genAI = new GoogleGenerativeAI(geminiApiKey);
        // Using gemini-1.5-flash as it's fast and suitable for this task
        const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });

        const prompt = `
        You are an expert technical recruiter and CV parser.
        Extract the following information from the resume text provided below and format it STRICTLY as a JSON object.
        Do not include markdown blocks or any other text outside the JSON.

        Required JSON structure:
        {
          "skills": ["List", "of", "technical", "and", "soft", "skills"],
          "education": "Brief summary of highest education (e.g., BSc Computer Science from University X)",
          "experience": "Brief summary of work experience, focusing on roles and years",
          "years_of_experience": Number (integer, estimate based on dates),
          "bio": "A professional summary or objective generated from the resume (2-3 sentences)",
          "github_url": "Extract GitHub URL if present, else empty string",
          "linkedin_url": "Extract LinkedIn URL if present, else empty string",
          "portfolio_url": "Extract any other portfolio website URL if present, else empty string"
        }

        Resume Text:
        """
        ${textContent}
        """
        `;

        const geminiResult = await model.generateContent(prompt);
        const responseText = geminiResult.response.text();
        
        // 5. Parse Gemini Response
        // Clean up markdown formatting if Gemini included it despite instructions
        let cleanJsonStr = responseText.replace(/```json/g, "").replace(/```/g, "").trim();
        
        let parsedData;
        try {
            parsedData = JSON.parse(cleanJsonStr);
        } catch (jsonError) {
             console.error("Failed to parse Gemini output as JSON:", responseText);
             throw new HttpsError("internal", "Gemini returned invalid JSON format.");
        }

        console.log("Gemini Parsing successful:", parsedData);

        // 6. Update Firestore Profiles
        const batch = db.batch();

        // Update CandidateProfile
        const profileRef = db.collection("users").document(userUid).collection("candidate_profile").document("profile");
        batch.set(profileRef, {
            skills: parsedData.skills || [],
            education: parsedData.education || "",
            experience: parsedData.experience || "",
            years_of_experience: parsedData.years_of_experience || 0,
            bio: parsedData.bio || "",
            github_url: parsedData.github_url || "",
            linkedin_url: parsedData.linkedin_url || "",
            portfolio_url: parsedData.portfolio_url || "",
            updated_at: new Date()
        }, { merge: true });

        // Update CV document status
        const cvRef = db.collection("cvs").document(cvId);
        batch.update(cvRef, {
            parsed: true,
            parsed_at: new Date()
        });

        // Mark user setup as complete
        const userRef = db.collection("users").document(userUid);
        batch.update(userRef, {
            setup_complete: true
        });

        await batch.commit();
        console.log("Firestore updated successfully.");

        return { success: true, message: "CV parsed and profile updated successfully." };

    } catch (error) {
        console.error("Error in parseCvWithGemini:", error);
        
        // Update CV document to reflect failure if possible
        try {
            await db.collection("cvs").document(cvId).update({
                parsed: false,
                parse_error: error.message || "Unknown error"
            });
        } catch (dbError) {
             console.error("Failed to update CV document with error status:", dbError);
        }

        if (error instanceof HttpsError) {
            throw error;
        }
        throw new HttpsError("internal", "An error occurred while parsing the CV.", error.message);
    }
});

// ─── TF-IDF Search Algorithm Utilities ─────────────────────────────────────

function _tokenize(text) {
    if (!text) return {};
    const lowerText = text.toLowerCase().trim();
    // Match words
    const words = lowerText.match(/\b\w+\b/g) || [];
    const stopWords = new Set(['the', 'a', 'an', 'is', 'are', 'was', 'were', 'be', 'been',
        'and', 'or', 'but', 'in', 'on', 'at', 'to', 'for', 'of', 'with',
        'by', 'from', 'as', 'into', 'through', 'during', 'before', 'after',
        'this', 'that', 'these', 'those', 'it', 'its', 'we', 'our', 'you', 'your']);
    
    const counts = {};
    for (const w of words) {
        if (!stopWords.has(w) && w.length > 1) {
            counts[w] = (counts[w] || 0) + 1;
        }
    }
    return counts;
}

function _cosineSimilarity(vec1, vec2) {
    let intersection = Object.keys(vec1).filter(k => vec2.hasOwnProperty(k));
    let numerator = 0;
    for (const k of intersection) {
        numerator += vec1[k] * vec2[k];
    }
    let sum1 = Object.values(vec1).reduce((a, b) => a + (b * b), 0);
    let sum2 = Object.values(vec2).reduce((a, b) => a + (b * b), 0);
    let denominator = Math.sqrt(sum1) * Math.sqrt(sum2);
    return denominator === 0 ? 0.0 : numerator / denominator;
}

function _skillOverlapScore(querySkills, targetSkills) {
    if (!querySkills || querySkills.length === 0) return 1.0;
    const qSet = new Set(querySkills.map(s => s.trim().toLowerCase()).filter(s => s));
    const tSet = new Set(targetSkills.map(s => s.trim().toLowerCase()).filter(s => s));
    
    if (qSet.size === 0) return 1.0;
    
    let overlap = 0;
    for (const qs of qSet) {
        if (tSet.has(qs)) overlap++;
    }
    return overlap / qSet.size;
}

function _recencyBoost(timestamp) {
    if (!timestamp) return 0.0;
    // timestamp could be a Firestore Timestamp object or Date
    const date = timestamp.toDate ? timestamp.toDate() : new Date(timestamp);
    const ageMs = Date.now() - date.getTime();
    const days = ageMs / (1000 * 60 * 60 * 24);
    
    if (days < 1) return 0.15;
    if (days < 7) return 0.10;
    if (days < 30) return 0.05;
    return 0.0;
}

// ─── Search Cloud Functions ────────────────────────────────────────────────

exports.searchJobs = onCall(async (request) => {
    if (!request.auth) throw new HttpsError("unauthenticated", "Must be logged in.");
    
    const data = request.data || {};
    const queryText = data.query || "";
    const skillsFilter = data.skills || []; // array of strings
    const minExperience = data.minExperience || 0;
    const location = data.location || "";

    try {
        const jobsSnapshot = await db.collection("jobs").where("status", "==", "active").get();
        const results = [];
        
        const queryVec = _tokenize(`${queryText} ${skillsFilter.join(" ")}`);

        jobsSnapshot.forEach(doc => {
            const job = doc.data();
            
            // 1. Initial Filtering
            if (location && (!job.location || !job.location.toLowerCase().includes(location.toLowerCase()))) {
                return; // skip
            }
            if (skillsFilter.length > 0) {
                const jobKeywords = (job.search_keywords_index || "").toLowerCase();
                const hasSkill = skillsFilter.some(s => jobKeywords.includes(s.toLowerCase()));
                if (!hasSkill) return; // Must have at least one matching skill if filtered
            }

            // 2. Scoring
            const jobText = `${job.title} ${job.company} ${job.description} ${(job.required_skills||[]).join(" ")}`;
            const jobVec = _tokenize(jobText);
            
            const textSim = (queryText || skillsFilter.length > 0) ? _cosineSimilarity(queryVec, jobVec) : 1.0;
            const skillScore = _skillOverlapScore(skillsFilter, job.required_skills || []);
            
            let expScore = 1.0;
            const jobMinExp = parseInt(job.experience_level) || 0; // assuming experience_level stores years or similar
            if (minExperience > 0 && jobMinExp > 0) {
                if (minExperience >= jobMinExp) expScore = 1.0;
                else expScore = minExperience / jobMinExp;
            }

            const recency = _recencyBoost(job.created_at);

            let finalScore = (0.4 * textSim) + (0.35 * skillScore) + (0.10 * expScore) + (0.15 * recency);
            let matchPct = Math.min(finalScore * 100, 99.9);

            if (!queryText && skillsFilter.length === 0) {
                matchPct = 100.0;
            }

            results.push({
                job: job, // Frontend maps this to DjangoJob
                match_percentage: parseFloat(matchPct.toFixed(1))
            });
        });

        results.sort((a, b) => b.match_percentage - a.match_percentage);
        return { results };

    } catch (e) {
        console.error(e);
        throw new HttpsError("internal", "Search failed.");
    }
});

exports.searchCandidates = onCall(async (request) => {
    if (!request.auth) throw new HttpsError("unauthenticated", "Must be logged in.");
    
    // Quick role check: optionally restrict to recruiters
    // But since it's a callable function, checking role is good practice
    
    const data = request.data || {};
    const querySkills = data.skills || []; // array
    const minExperience = data.minExperience || 0;
    const degree = data.degree || "";

    try {
        const usersSnapshot = await db.collection("users").get();
        const results = [];
        
        const queryVec = _tokenize(querySkills.join(" "));

        // Since profiles are in subcollections, we need to fetch them
        // For performance in a real app, you'd index these or keep a searchable copy on the user doc
        for (const userDoc of usersSnapshot.docs) {
            const userData = userDoc.data();
            if (userData.role !== "student" || !userData.setup_complete) continue;
            
            const profileSnap = await userDoc.ref.collection("candidate_profile").document("profile").get();
            if (!profileSnap.exists) continue;
            
            const profile = profileSnap.data();

            // 1. Filter
            if (minExperience > 0 && (profile.years_of_experience || 0) < minExperience) continue;
            if (degree && (!profile.education || !profile.education.toLowerCase().includes(degree.toLowerCase()))) continue;
            
            if (querySkills.length > 0) {
                const candSkillsStr = (profile.skills || []).join(" ").toLowerCase();
                const hasSkill = querySkills.some(s => candSkillsStr.includes(s.toLowerCase()));
                if (!hasSkill) continue;
            }

            // 2. Score
            const skillScore = _skillOverlapScore(querySkills, profile.skills || []);
            
            const candText = `${profile.education} ${(profile.skills||[]).join(" ")} ${profile.experience} ${profile.bio}`;
            const candVec = _tokenize(candText);
            const textSim = querySkills.length > 0 ? _cosineSimilarity(queryVec, candVec) : 1.0;
            
            const candExp = profile.years_of_experience || 0;
            let expScore = 1.0;
            if (minExperience > 0 && candExp > 0) {
                expScore = Math.min(candExp / Math.max(minExperience, 1), 1.5) / 1.5;
            }

            const completeness = (userData.setup_complete) ? 1.0 : 0.5; // simple metric
            const recency = _recencyBoost(profile.updated_at || userData.created_at);

            let finalScore = (0.35 * skillScore) + (0.25 * textSim) + (0.15 * expScore) + (0.10 * completeness) + (0.15 * recency);
            let matchPct = Math.min(finalScore * 100, 99.9);

            if (querySkills.length === 0) matchPct = 100.0;

            results.push({
                candidate: {
                    uid: userData.uid,
                    email: userData.email,
                    username: userData.username,
                    photo_url: userData.photo_url || "",
                    ...profile
                },
                match_percentage: parseFloat(matchPct.toFixed(1))
            });
        }

        results.sort((a, b) => b.match_percentage - a.match_percentage);
        return { results };

    } catch (e) {
        console.error(e);
        throw new HttpsError("internal", "Search failed.");
    }
});
