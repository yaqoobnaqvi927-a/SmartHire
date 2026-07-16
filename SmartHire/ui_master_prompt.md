# SmartHire UI Master Prompt

You can copy and paste the text below into your UI generation tool (such as v0 by Vercel, Lovable, Bolt.new, Cursor, or Google IDX).

***

**System Role & Objective:**
You are an expert Frontend Developer and UX/UI Designer. Your task is to generate the complete frontend user interface for **SmartHire**, an AI-powered job matching and recruitment platform. You must output highly functional, responsive, and visually stunning code using modern web technologies (e.g., React, Next.js, Tailwind CSS, Framer Motion for animations, and Lucide React for icons).

### 1. Design Aesthetics & Vibe
Create an interface that feels extremely premium, modern, and state-of-the-art. DO NOT use generic or plain styles.
*   **Theme:** Sleek Dark Mode by default, utilizing a deep rich background (e.g., `#0F172A` or `#09090b`).
*   **Color Palette:** 
    *   Primary: Electric Indigo (`#4F46E5` to `#6366F1`)
    *   Accent/Secondary: Neon Cyan (`#06B6D4` to `#22D3EE`) for AI-driven elements (Match Scores, AI Analysis).
    *   Success: Emerald Green (`#10B981`)
    *   Warning: Amber (`#F59E0B`)
*   **Typography:** Modern Sans-Serif like `Inter`, `Outfit`, or `Plus Jakarta Sans`.
*   **Visual Styling:** 
    *   Extensive use of **Glassmorphism** (translucent backgrounds with blur effects) for cards, modals, and navbars.
    *   Smooth gradients on buttons and active states.
    *   Subtle borders (`border-white/10`) to define elements in the dark theme.
*   **Interactivity:** Incorporate micro-animations for all interactive elements (hover effects, smooth page transitions, staggered list appearances).

### 2. Backend Context & Data Models
The UI must perfectly map to the existing Django backend schema. Here is the exact data structure the UI will consume:

*   **Users:** Two roles (`student`/`recruiter`). Includes `full_name`, `username`.
*   **Candidate Profile:** `total_experience`, `extracted_skills` (array), `degree_extracted`, `cv_file_path`.
*   **Recruiter Profile:** `company_name`, `company_size`, `industry`.
*   **CV Bank:** Multiple CVs per candidate. Includes `file`, `parsed_experience`, `parsed_education` (JSON array), `skills_extracted`, `is_primary` flag.
*   **Job Postings:** `title`, `required_skills` (array), `min_experience`, `job_type` (e.g., onsite, remote), `status` (active/closed).
*   **Applications:** Links Job and Candidate. Includes `ai_match_score` (float), `ats_status` (new, reviewed, shortlisted, rejected), `skill_gap_analysis` (JSON array).
*   **Interviews:** Links to Application. Includes `scheduled_datetime`, `zoom_meeting_link`, `status`.
*   **Communications:** `Notification` (title, message, is_read). `ChatThread` (links Job, Candidate, Recruiter) and `ChatMessage` (content, timestamp, sender).

### 3. Required Screens & Elements to Generate

#### A. Authentication & Onboarding
*   **Login/Signup Screen:** Split screen or glassmorphic modal. Must include a clear toggle/selector between "I am a Candidate" and "I am a Recruiter".
*   **Recruiter Onboarding:** Form for `company_name`, `company_size`, and `industry`.
*   **Candidate Onboarding (CV Upload):** A beautiful drag-and-drop zone for PDF uploads. Include a loading state with a dynamic "AI Parsing CV..." animation that then displays the extracted data (`parsed_experience`, `parsed_education`, `skills_extracted`) for the user to confirm.

#### B. Candidate Portal (Role: Student)
*   **Candidate Dashboard:** 
    *   Overview widgets: "Active Applications", "Upcoming Interviews".
    *   "My Profile" card showing extracted skills as tags, total experience, and primary CV status.
*   **Job Search & Discovery Page:** 
    *   List/Grid of `JobPosting`s. 
    *   **Crucial:** Visually highlight the `ai_match_score` (e.g., a circular progress ring in cyan/green) next to each job. 
    *   Include a "Skill Gap Analysis" popover showing what skills the candidate is missing for that specific job.
*   **My Applications Tracker:** A Kanban board or a sleek timeline list showing the `ats_status` of each application. Include a section for `ScheduledInterview`s with a prominent "Join Zoom" button.

#### C. Recruiter Portal (Role: Recruiter)
*   **Recruiter Dashboard:** Metrics on active jobs, total applications received, and recent notifications.
*   **Job Creation Form:** Input fields for `title`, `job_type`, `min_experience`, and a dynamic tag input for `required_skills`.
*   **ATS (Applicant Tracking System) View:**
    *   A dashboard for a specific job posting.
    *   A list of applied candidates, strictly sorted by `ai_match_score`.
    *   **Candidate Card:** Shows the candidate's name, degree, experience, and the AI Match Score ring.
    *   **Expandable Details:** Clicking a candidate reveals their `skill_gap_analysis` and parsed CV details.
    *   **Action Bar:** Buttons to change `ats_status` (Shortlist, Reject) and a button to "Schedule Interview" (opens a modal to input `scheduled_datetime` and `zoom_meeting_link`).

#### D. Global Components (Both Roles)
*   **Real-time Chat Interface:** A sleek sliding drawer or dedicated page. Left sidebar with `ChatThread`s (showing Job Title and counterpart's name). Main area showing `ChatMessage` bubbles.
*   **Notification Bell & Dropdown:** Top navbar element. Shows unread count. Dropdown lists `Notification` items (e.g., "Interview Scheduled", "Application Status Updated").
*   **Navigation Sidebar/Navbar:** Glassmorphic sidebar with Lucide icons (Home, Jobs, Applications, Messages, Settings).

**Instruction for the AI:** Start by generating the **Candidate Dashboard and Job Discovery Page** showcasing the AI Match Score UI and Glassmorphic aesthetics. Ensure all components are modular and ready to be hooked up to REST API endpoints matching the provided Django models.
