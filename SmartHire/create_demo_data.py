import os
import random
from django.utils import timezone

def create_demo_data():
    from django.contrib.auth import get_user_model
    from users.models import RecruiterProfile, CandidateProfile
    from jobs.models import JobPosting, Application
    User = get_user_model()
    
    print('Clearing old demo data...')
    JobPosting.objects.filter(title__contains='(Demo)').delete()
    
    print('Creating Recruiter...')
    recruiter_user, _ = User.objects.get_or_create(username='demo_recruiter', email='recruiter@demo.com', role_type='recruiter')
    recruiter_user.set_password('demo1234')
    recruiter_user.save()
    
    RecruiterProfile.objects.get_or_create(
        user=recruiter_user,
        defaults={
            'company_name': 'TechNova Solutions',
            'position': 'Senior Tech Lead',
            'company_website': 'www.technova.com'
        }
    )

    print('Creating Candidate...')
    student_user, _ = User.objects.get_or_create(username='demo_student', email='student@demo.com', role_type='student')
    student_user.set_password('demo1234')
    student_user.save()

    CandidateProfile.objects.get_or_create(
        user=student_user,
        defaults={
            'degree_extracted': 'BS Computer Science',
            'total_experience': 2,
            'extracted_skills_json': ['python', 'django', 'kotlin', 'android', 'git'],
            'bio': 'Passionate Android and Python developer looking for exciting opportunities.'
        }
    )

    print('Creating highly detailed Job Postings...')
    jobs = [
        {
            'title': 'Senior Android Engineer (Demo)',
            'company': 'Google',
            'location': 'Mountain View, CA',
            'description': 'We are looking for an expert Android developer with strong knowledge of Kotlin, Jetpack Compose, and Coroutines. You will build highly scalable consumer applications.',
            'required_skills_json': ['kotlin', 'android', 'jetpack compose', 'coroutines', 'firebase'],
            'job_type': 'full-time',
            'experience_level': 'senior'
        },
        {
            'title': 'Backend Python Developer (Demo)',
            'company': 'Meta',
            'location': 'Remote',
            'description': 'Join our infrastructure team. You need strong skills in Django, REST APIs, PostgreSQL, and AWS.',
            'required_skills_json': ['python', 'django', 'postgresql', 'aws', 'docker'],
            'job_type': 'full-time',
            'experience_level': 'mid'
        },
        {
            'title': 'Machine Learning Engineer (Demo)',
            'company': 'OpenAI',
            'location': 'San Francisco, CA',
            'description': 'Help us build the next generation of LLMs. Requires deep knowledge of PyTorch, NLP, and Transformer architectures.',
            'required_skills_json': ['python', 'pytorch', 'nlp', 'transformers', 'machine learning'],
            'job_type': 'full-time',
            'experience_level': 'expert'
        }
    ]

    for job_data in jobs:
        JobPosting.objects.get_or_create(
            recruiter=recruiter_user,
            title=job_data['title'],
            defaults={
                'company': job_data['company'],
                'location': job_data['location'],
                'description': job_data['description'],
                'required_skills_json': job_data['required_skills_json'],
                'job_type': job_data['job_type'],
                'experience_level': job_data['experience_level'],
                'status': 'active'
            }
        )
    print('Successfully populated realistic FYP Demo Data!')

if __name__ == '__main__':
    import django
    os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'smarthire_backend.settings.production')
    django.setup()
    create_demo_data()
