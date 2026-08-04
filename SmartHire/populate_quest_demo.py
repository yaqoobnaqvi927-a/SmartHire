import os
import django
import random
from django.utils import timezone

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'smarthire_backend.settings.development')
try:
    django.setup()
except Exception:
    pass

from django.contrib.auth import get_user_model
from users.models import RecruiterProfile, CandidateProfile
from jobs.models import JobPosting, Application

User = get_user_model()

def populate_quest_data():
    print("Populating QUEST Nawabshah Demonstration Data...")

    # 1. Recruiter User & Profile
    recruiter_user, _ = User.objects.get_or_create(
        username='quest_recruiter',
        defaults={
            'email': 'placement@quest.edu.pk',
            'role_type': 'recruiter',
            'full_name': 'QUEST Placement Officer'
        }
    )
    recruiter_user.set_password('demo1234')
    recruiter_user.save()

    recruiter_profile, _ = RecruiterProfile.objects.get_or_create(
        user=recruiter_user,
        defaults={
            'company_name': 'QUEST Nawabshah Career Hub',
            'company_website': 'https://quest.edu.pk',
            'industry': 'Higher Education & Technology Placement'
        }
    )

    # 2. QUEST Student Candidates (from C:\Users\yaqoo\Downloads\CVS)
    candidates_data = [
        {
            'username': 'aliza_arshad',
            'email': 'alizaarshad2709@gmail.com',
            'full_name': 'Aliza Arshad',
            'degree': 'B.E. Computer Systems Engineering (QUEST Nawabshah)',
            'experience': 1,
            'skills': ['Cybersecurity', 'Network Security', 'Azure', 'Machine Learning', 'Generative AI', 'Python'],
            'bio': 'Computer Systems Engineering student at QUEST Nawabshah specializing in Network Security, Azure Cloud, and Machine Learning.'
        },
        {
            'username': 'fiza_memon',
            'email': 'fizafahim934@gmail.com',
            'full_name': 'Fiza Memon',
            'degree': 'B.E. Computer Systems Engineering (QUEST Nawabshah)',
            'experience': 1,
            'skills': ['Frontend Development', 'React', 'JavaScript', 'HTML5', 'CSS3', 'UI/UX Design', 'Git'],
            'bio': 'Passionate Frontend Web Developer & UI/UX Designer from QUEST Nawabshah with expertise in responsive web design and React.'
        },
        {
            'username': 'kanchan_kumari',
            'email': 'kanchan.k1@gmail.com',
            'full_name': 'Kanchan Kumari',
            'degree': 'B.E. Computer Systems Engineering (QUEST Nawabshah)',
            'experience': 1,
            'skills': ['Python', 'Pandas', 'NumPy', 'Machine Learning', 'MySQL', 'C++', 'Tailwind CSS'],
            'bio': 'Data Science & Software Developer at QUEST Nawabshah skilled in Python data analysis, machine learning models, and database systems.'
        },
        {
            'username': 'khushboo_hattar',
            'email': 'khushboohattar15@gmail.com',
            'full_name': 'Khushboo Hattar',
            'degree': 'Bachelor of Computer Systems (QUEST Nawabshah)',
            'experience': 1,
            'skills': ['React.js', 'Node.js', 'Express', 'JavaScript', 'HTML5', 'CSS3', 'REST API'],
            'bio': 'Fullstack MERN Stack Developer from QUEST Nawabshah with hands-on experience building modern web applications.'
        }
    ]

    candidate_objs = []
    for cand in candidates_data:
        user, _ = User.objects.get_or_create(
            username=cand['username'],
            defaults={
                'email': cand['email'],
                'role_type': 'student',
                'full_name': cand['full_name']
            }
        )
        user.set_password('demo1234')
        user.save()

        profile, _ = CandidateProfile.objects.get_or_create(
            user=user,
            defaults={
                'degree_extracted': cand['degree'],
                'total_experience': cand['experience'],
                'extracted_skills_json': cand['skills'],
                'bio': cand['bio'],
                'location': 'Nawabshah, Sindh',
                'is_searchable': True
            }
        )
        candidate_objs.append(profile)

    # 3. Matching Job Postings
    jobs_data = [
        {
            'title': 'Cybersecurity & Network Engineer (QUEST Campus Drive)',
            'company': 'CyberShield Networks',
            'location': 'Nawabshah / Remote',
            'description': 'We are recruiting Computer Systems graduates from QUEST Nawabshah for Cloud Network Security, Azure Infrastructure, and Security Auditing.',
            'skills': ['Cybersecurity', 'Network Security', 'Azure', 'Python'],
            'job_type': 'full-time',
            'experience_level': 'entry'
        },
        {
            'title': 'Frontend React Developer (QUEST Campus Drive)',
            'company': 'TechNova Solutions',
            'location': 'Karachi / Remote',
            'description': 'Hiring Frontend Web Developers skilled in React, JavaScript, HTML5/CSS3, and UI/UX design.',
            'skills': ['Frontend Development', 'React', 'JavaScript', 'HTML5', 'CSS3', 'Git'],
            'job_type': 'full-time',
            'experience_level': 'entry'
        },
        {
            'title': 'Data Analyst & ML Developer (QUEST Campus Drive)',
            'company': 'DataMind AI',
            'location': 'Remote / Nawabshah',
            'description': 'Looking for entry-level Machine Learning Engineers and Data Analysts proficient in Python, Pandas, NumPy, and MySQL.',
            'skills': ['Python', 'Pandas', 'NumPy', 'Machine Learning', 'MySQL'],
            'job_type': 'full-time',
            'experience_level': 'entry'
        },
        {
            'title': 'Fullstack MERN Web Engineer (QUEST Campus Drive)',
            'company': 'CloudScale Labs',
            'location': 'Remote',
            'description': 'Hiring Fullstack Web Developers experienced with React.js, Node.js, Express, and REST APIs.',
            'skills': ['React.js', 'Node.js', 'Express', 'JavaScript', 'REST API'],
            'job_type': 'full-time',
            'experience_level': 'entry'
        }
    ]

    job_objs = []
    for j_data in jobs_data:
        job, _ = JobPosting.objects.get_or_create(
            recruiter=recruiter_profile,
            title=j_data['title'],
            defaults={
                'company': j_data['company'],
                'location': j_data['location'],
                'description': j_data['description'],
                'required_skills_json': j_data['skills'],
                'job_type': j_data['job_type'],
                'status': 'active'
            }
        )
        job_objs.append(job)

    # 4. Connect Applications (Candidate -> Job)
    for cand_profile, job in zip(candidate_objs, job_objs):
        Application.objects.get_or_create(
            candidate=cand_profile,
            job=job,
            defaults={
                'ats_status': 'screened',
                'ai_match_score': 92.5
            }
        )

    print("QUEST Nawabshah Demonstration Data Populated Successfully!")

if __name__ == '__main__':
    populate_quest_data()
