import os
import random
import datetime
import django
from django.utils import timezone

# Configure Django settings
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'smarthire_backend.settings.development')
django.setup()

from django.contrib.auth import get_user_model
from users.models import RecruiterProfile, CandidateProfile
from jobs.models import JobPosting, Application
from ai_engine.models import AIMatchReport
from interviews.models import Interview
from communications.models import ChatThread, ChatMessage

User = get_user_model()

def seed_database():
    print("Starting database seeding...")

    # 1. Clear existing data
    print("Clearing old jobs, applications, interviews, and chat histories...")
    ChatMessage.objects.all().delete()
    ChatThread.objects.all().delete()
    Interview.objects.all().delete()
    AIMatchReport.objects.all().delete()
    Application.objects.all().delete()
    JobPosting.objects.all().delete()

    # 2. Retrieve recruiters and candidates
    recruiters = list(RecruiterProfile.objects.all())
    candidates = list(CandidateProfile.objects.all())

    if not recruiters:
        print("Error: No recruiters found in the database. Please register at least one recruiter first.")
        return

    if not candidates:
        print("Error: No candidates found in the database. Please register at least one candidate first.")
        return

    print(f"Found {len(recruiters)} recruiters and {len(candidates)} candidates.")

    # 3. Define templates for generating 150+ jobs
    companies = [
        "Google", "Microsoft", "Meta", "Netflix", "Amazon", "Stripe", "Shopify", 
        "Slack", "Uber", "Spotify", "Canva", "Figma", "Zoom", "Airbnb", 
        "TikTok", "Salesforce", "Atlassian", "Twilio", "Adyen", "Stripe", 
        "Oracle", "Intel", "Qualcomm", "NVIDIA", "Dell", "IBM", "Dropbox", "Vercel"
    ]

    job_types = ["remote", "onsite", "hybrid"]
    locations = [
        "San Francisco, CA", "Seattle, WA", "New York, NY", "Austin, TX", 
        "London, UK", "Karachi, Pakistan", "Lahore, Pakistan", "Berlin, Germany", 
        "Toronto, Canada", "Singapore", "Sydney, Australia", "Dublin, Ireland",
        "Amsterdam, Netherlands", "Remote", "Chicago, IL", "Boston, MA"
    ]

    degrees = ["Bachelors", "Masters", "PhD", "Not Specified"]

    job_categories = [
        {
            "titles": ["Backend Engineer", "Python Developer", "Django Architect", "Staff Backend Developer"],
            "skills": ["Python", "Django", "Postgresql", "Rest Api", "Docker", "Git"],
            "desc": "Join our platform team to build scalable services and microservices. You will work on building clean REST APIs, optimize database queries, and establish seamless data flows using Docker and PostgreSQL."
        },
        {
            "titles": ["Frontend Developer", "React Engineer", "UI Specialist", "Senior Frontend Engineer"],
            "skills": ["Javascript", "React", "Typescript", "Next.Js", "Figma", "Git"],
            "desc": "Help us build beautiful, accessible user interfaces. In this role, you will translate high-fidelity Figma prototypes into clean React components, ensuring performance, responsiveness, and state optimization."
        },
        {
            "titles": ["Android Engineer", "Mobile App Developer", "Kotlin Specialist", "Senior Android Developer"],
            "skills": ["Kotlin", "Android", "Jetpack Compose", "Rest Api", "Git"],
            "desc": "We are seeking a developer to build feature-rich Android apps. You will collaborate on developing clean Compose UIs, integrating backend services, and ensuring native performance and high responsiveness."
        },
        {
            "titles": ["Flutter Engineer", "Cross-Platform Developer", "Mobile Architect"],
            "skills": ["Dart", "Flutter", "Android", "Ios", "Rest Api"],
            "desc": "Build cross-platform mobile apps for iOS and Android. You will manage state using modern architectural patterns and implement custom interactive features."
        },
        {
            "titles": ["Data Scientist", "Data Analyst", "Applied Statistician"],
            "skills": ["Python", "Machine Learning", "Sql", "Pandas", "Numpy", "Scikit-Learn"],
            "desc": "Use data to drive decisions and products. You will build and evaluate predictive models, clean complex datasets, and present actionable insights to product teams."
        },
        {
            "titles": ["Machine Learning Engineer", "AI Researcher", "Deep Learning Scientist"],
            "skills": ["Python", "Pytorch", "Tensorflow", "Machine Learning", "Deep Learning", "Llm"],
            "desc": "Design and deploy machine learning models in production. You will work with LLMs, build neural networks, and optimize model inference for scalability."
        },
        {
            "titles": ["DevOps Engineer", "Site Reliability Engineer (SRE)", "Infrastructure Architect"],
            "skills": ["Docker", "Kubernetes", "Aws", "Ci/Cd", "Terraform", "Git"],
            "desc": "Automate infrastructure deployment and manage system reliability. You will design CI/CD pipelines, configure Kubernetes clusters, and scale cloud infrastructure on AWS."
        },
        {
            "titles": ["Product Designer", "UI/UX Designer", "Interaction Designer"],
            "skills": ["Figma", "Adobe Xd", "Product Design", "Teamwork"],
            "desc": "Craft intuitive experiences for our users. You will conduct user research, design wireframes, build high-fidelity interactive mockups, and collaborate closely with engineers."
        },
        {
            "titles": ["Product Manager", "Associate Product Manager", "Scrum Master"],
            "skills": ["Agile", "Scrum", "Project Management", "Jira", "Leadership"],
            "desc": "Own the roadmap of our core developer tools. You will lead standups, manage sprint backlogs, define user requirements, and translate business vision into engineering tasks."
        },
        {
            "titles": ["QA Engineer", "Automation QA Specialist", "Test Engineer"],
            "skills": ["Selenium", "Python", "Problem Solving", "Git"],
            "desc": "Maintain software quality through manual and automated test suites. You will write automated end-to-end tests using Selenium and document critical software issues."
        },
        {
            "titles": ["HR Manager", "Talent Acquisition Specialist", "Recruiter"],
            "skills": ["Leadership", "Communication", "Teamwork", "Project Management"],
            "desc": "Help us discover, hire, and support exceptional human beings. You will lead hiring campaigns, screen applicants, and coordinate interview loops."
        },
        {
            "titles": ["Cyber Security Analyst", "Security Engineer", "Penetration Tester"],
            "skills": ["Cyber Security", "Network Security", "Docker", "Problem Solving"],
            "desc": "Secure our corporate network and cloud systems. You will perform penetration tests, audit access controls, and design security policies for engineering teams."
        },
        {
            "titles": ["Data Engineer", "Big Data Developer", "Analytics Engineer"],
            "skills": ["Python", "Sql", "Kafka", "Spark", "Data Pipeline"],
            "desc": "Build scalable data warehouses and real-time streaming pipelines. You will design ETL jobs, ingest data from third-party APIs, and organize analytical schemas."
        }
    ]

    salary_ranges = [
        "$50,000 - $70,000", "$70,000 - $90,000", "$90,000 - $120,000", 
        "$120,000 - $150,000", "$150,000 - $180,000", "$180,000 - $220,000",
        "100k - 130k PKR", "150k - 250k PKR", "300k - 450k PKR"
    ]

    # Generate 155 Job Postings
    jobs = []
    print("Generating 155 Job Postings...")
    for i in range(155):
        category = random.choice(job_categories)
        title = random.choice(category["titles"])
        company = random.choice(companies)
        
        # Suffix a tier or department to make title unique
        title_suffix = random.choice(["", " (Senior)", " (Lead)", " - Core Team", " - Remote Services", " (Junior)"])
        full_title = f"{title}{title_suffix}".strip()
        
        recruiter = random.choice(recruiters)
        skills_req = category["skills"]
        # Randomly omit or add some soft skills
        if random.random() > 0.5:
            skills_req = list(set(skills_req + [random.choice(["Leadership", "Communication", "Teamwork", "Problem Solving"])]))

        min_exp = random.randint(0, 7)
        degree_req = random.choice(degrees)
        job_type = random.choice(job_types)
        location = "Remote" if job_type == "remote" else random.choice(locations)
        salary = random.choice(salary_ranges)

        description = f"{category['desc']}\n\nKey Responsibilities:\n- Collaborate with developers to build quality products.\n- Solve complex architectural bugs.\n- Document designs and code decisions."

        job = JobPosting(
            recruiter=recruiter,
            title=full_title,
            company=company,
            description=description,
            required_skills_json=skills_req,
            min_experience=min_exp,
            degree_requirement=degree_req,
            job_type=job_type,
            location=location,
            salary_range=salary,
            status="active"
        )
        job.save()
        jobs.append(job)

    print(f"Successfully generated {len(jobs)} job postings.")

    # 4. Generate Applications
    print("Generating Applications & ATS pipelines...")
    stages = ["new", "screened", "interview", "offer", "hired", "rejected"]
    
    # We will pick a subset of jobs to receive applications
    active_job_subset = random.sample(jobs, min(len(jobs), 45))
    applications = []

    for idx, job in enumerate(active_job_subset):
        # Pick 1 to 3 random candidates to apply for this job
        candidates_to_apply = random.sample(candidates, random.randint(1, 3))
        
        for candidate in candidates_to_apply:
            if Application.objects.filter(job=job, candidate=candidate).exists():
                continue

            # Calculate a realistic match score based on candidate skills vs job skills
            cand_skills = set(s.lower() for s in candidate.extracted_skills_json) if isinstance(candidate.extracted_skills_json, list) else set()
            job_skills = set(s.lower() for s in job.required_skills_json) if isinstance(job.required_skills_json, list) else set()
            
            matched = list(cand_skills & job_skills)
            missing = list(job_skills - cand_skills)
            
            if job_skills:
                skill_match_pct = (len(matched) / len(job_skills)) * 100.0
            else:
                skill_match_pct = 100.0

            exp_match = (candidate.total_experience or 0) >= job.min_experience
            exp_score = 100.0 if exp_match else ((candidate.total_experience or 0) / max(job.min_experience, 1)) * 100.0
            
            # Weighted overall score
            ai_match_score = round((skill_match_pct * 0.7) + (exp_score * 0.3), 1)

            # Pick status
            # Higher match score means higher chance of progressing
            if ai_match_score >= 80:
                status = random.choice(["interview", "offer", "hired", "screened"])
            elif ai_match_score >= 60:
                status = random.choice(["new", "screened", "interview", "rejected"])
            else:
                status = random.choice(["new", "rejected"])

            cover_letter = f"Dear Hiring Manager,\n\nI am writing to apply for the position of {job.title} at {job.company}. My background in {', '.join(list(cand_skills)[:4])} makes me a strong fit. I have {candidate.total_experience} years of experience and hold a {candidate.degree_extracted} degree.\n\nBest regards,\n{(candidate.user.full_name or candidate.user.username)}"

            app = Application(
                job=job,
                candidate=candidate,
                ai_match_score=ai_match_score,
                ats_status=status,
                skill_gap_analysis=missing,
                cover_letter=cover_letter
            )
            app.save()
            applications.append(app)

            # Create the corresponding AIMatchReport (cached AI analysis)
            recommendation = ""
            if not missing:
                recommendation = f"You are an exceptional fit for the {job.title} position at {job.company}! Your skills match 100% of the requirements. Make sure to prepare your technical portfolio and focus on architectural interview questions."
            else:
                recommendation = f"You have a strong match of {round(skill_match_pct, 1)}% for this role. To stand out, consider building a quick project focusing on your missing skills: {', '.join(missing[:3])}. We also recommend looking at resources for {missing[0]} to bridge the knowledge gap."

            report = AIMatchReport(
                application=app,
                match_score=ai_match_score,
                skill_match_pct=round(skill_match_pct, 1),
                experience_match=exp_match,
                matched_skills=matched,
                missing_skills=missing,
                recommendation=recommendation
            )
            report.save()

            # If the application is in "interview" status, schedule an interview
            if status == "interview":
                interview_types = ["video", "phone", "technical"]
                scheduled_time = timezone.now() + datetime.timedelta(days=random.randint(1, 10), hours=random.randint(9, 17))
                
                iv = Interview(
                    application=app,
                    scheduled_at=scheduled_time,
                    interview_type=random.choice(interview_types),
                    meeting_link="https://zoom.us/j/584063351495",
                    zoom_meeting_id="584 063 351495",
                    zoom_host_url="https://zoom.us/s/584063351495",
                    zoom_password="fyp_smarthire",
                    notes="Bring your portfolio and be prepared to discuss your previous projects.",
                    status="scheduled",
                    duration_minutes=45
                )
                iv.save()

            # Create a ChatThread and 2-3 ChatMessages between Recruiter and Candidate
            recruiter_user = job.recruiter.user
            candidate_user = candidate.user

            thread = ChatThread(
                job=job,
                candidate=candidate,
                recruiter=job.recruiter
            )
            thread.save()

            # Add Chat Messages
            msg1 = ChatMessage(
                thread=thread,
                sender=recruiter_user,
                content=f"Hi {candidate_user.full_name or candidate_user.username}, thanks for applying to the {job.title} role! We liked your profile. Are you free to connect this week?",
                timestamp=timezone.now() - datetime.timedelta(hours=5)
            )
            msg1.save()

            msg2 = ChatMessage(
                thread=thread,
                sender=candidate_user,
                content="Hi! Thank you for reaching out. Yes, I am absolutely available. I'm free on Wednesday and Thursday afternoons. Looking forward to talking!",
                timestamp=timezone.now() - datetime.timedelta(hours=4)
            )
            msg2.save()

            if status in ["interview", "offer", "hired"]:
                msg3 = ChatMessage(
                    thread=thread,
                    sender=recruiter_user,
                    content="Perfect. I have scheduled a video interview for us. You should see the details in your dashboard panel.",
                    timestamp=timezone.now() - datetime.timedelta(hours=3)
                )
                msg3.save()

    print(f"Successfully generated {len(applications)} applications, {Interview.objects.count()} interviews, and {ChatThread.objects.count()} chat threads.")

    # Re-calculate job counts
    for job in JobPosting.objects.all():
        job.update_counts()

    print("Database seeding completed successfully!")

if __name__ == "__main__":
    seed_database()
