import urllib.request
import urllib.error
import json
import random

BASE_URL = "https://yaqoob9227.pythonanywhere.com/"

def make_request(endpoint, data=None, token=None, method="POST"):
    url = BASE_URL + endpoint
    headers = {'Content-Type': 'application/json', 'User-Agent': 'Mozilla/5.0'}
    if token:
        headers['Authorization'] = f'Bearer {token}'
    
    if data is not None:
        data_bytes = json.dumps(data).encode('utf-8')
    else:
        data_bytes = None
        
    req = urllib.request.Request(url, data=data_bytes, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as response:
            content = response.read().decode('utf-8')
            if content:
                return json.loads(content)
            return {}
    except urllib.error.HTTPError as e:
        # Ignore 400s if it's just "username already exists" so the script doesn't fail on re-runs
        err_msg = e.read().decode('utf-8')
        print(f"Notice on {endpoint}: {e.code} - {err_msg}")
        return None
    except Exception as e:
        print(f"Error on {endpoint}: {e}")
        return None

def main():
    print("Starting data population on LIVE server (yaqoob9227.pythonanywhere.com)...")
    
    # 1. Register a Recruiter
    recruiter_data = {
        "username": "techcorp_hr",
        "email": "hr@techcorp.com",
        "password": "Password123!",
        "role_type": "recruiter",
        "first_name": "Alice",
        "last_name": "Smith"
    }
    print("Registering Recruiter 'techcorp_hr'...")
    make_request("api/users/register/", recruiter_data)
    
    auth = make_request("api/users/login/", {"username": "techcorp_hr", "password": "Password123!"})
    if not auth:
        print("Failed to login recruiter. They might already exist, attempting login anyway.")
    
    recruiter_token = auth['access'] if auth else None
    
    if recruiter_token:
        # Setup Recruiter Profile
        print("Setting up recruiter profile...")
        make_request("api/users/profile/setup/", {
            "company_name": "TechCorp Innovations",
            "industry": "Software Engineering",
            "company_size": "100-500"
        }, token=recruiter_token, method="PUT")
        
        # 2. Post some Jobs
        jobs_to_create = [
            {
                "title": "Senior Android Developer",
                "description": "Looking for an experienced Android developer to lead our mobile team. Must have strong Kotlin and Compose skills.",
                "skills_required": "Kotlin, Android SDK, Jetpack Compose, Retrofit",
                "min_experience": 4,
                "job_type": "remote",
                "location": "New York, NY",
                "salary_range": "$120k - $150k"
            },
            {
                "title": "Backend Python Engineer",
                "description": "Join our backend team building high-performance APIs with Django and PostgreSQL.",
                "skills_required": "Python, Django, PostgreSQL, REST APIs",
                "min_experience": 2,
                "job_type": "onsite",
                "location": "San Francisco, CA",
                "salary_range": "$100k - $130k"
            },
            {
                "title": "UI/UX Designer",
                "description": "We need a creative designer to revamp our user interfaces and create stunning prototypes.",
                "skills_required": "Figma, Adobe XD, UI/UX, Prototyping",
                "min_experience": 1,
                "job_type": "hybrid",
                "location": "Austin, TX",
                "salary_range": "$80k - $110k"
            },
            {
                "title": "Data Scientist (AI/ML)",
                "description": "Help us build the next generation of AI matching algorithms. Experience with scikit-learn and TensorFlow required.",
                "skills_required": "Python, Machine Learning, TensorFlow, Data Science",
                "min_experience": 3,
                "job_type": "remote",
                "location": "Boston, MA",
                "salary_range": "$130k - $160k"
            }
        ]
        
        created_jobs = []
        print("Posting realistic Jobs...")
        for job in jobs_to_create:
            res = make_request("api/jobs/jobs/", job, token=recruiter_token)
            if res and 'id' in res:
                created_jobs.append(res['id'])
                
    else:
        created_jobs = []
        print("Skipping job creation because recruiter login failed.")
            
    # 3. Register Candidates
    candidates = [
        {
            "user": {
                "username": "john_dev_2",
                "email": "john2@example.com",
                "password": "Password123!",
                "role_type": "student",
                "first_name": "John",
                "last_name": "Doe"
            },
            "profile": {
                "skills": "Python, Django, REST APIs, Git",
                "degree": "BSc Computer Science",
                "university": "State University",
                "expected_graduation": "2024"
            }
        },
        {
            "user": {
                "username": "sarah_mobile_2",
                "email": "sarah2@example.com",
                "password": "Password123!",
                "role_type": "student",
                "first_name": "Sarah",
                "last_name": "Connor"
            },
            "profile": {
                "skills": "Kotlin, Java, Android Studio, UI/UX",
                "degree": "BEng Software Engineering",
                "university": "Tech Institute",
                "expected_graduation": "2023"
            }
        },
        {
            "user": {
                "username": "mike_data_2",
                "email": "mike2@example.com",
                "password": "Password123!",
                "role_type": "student",
                "first_name": "Mike",
                "last_name": "Tyson"
            },
            "profile": {
                "skills": "Data Analysis, Python, SQL, Tableau",
                "degree": "BSc Data Science",
                "university": "City College",
                "expected_graduation": "2025"
            }
        }
    ]
    
    print("Registering Candidates and generating job applications...")
    for cand in candidates:
        make_request("api/users/register/", cand['user'])
        c_auth = make_request("api/users/login/", {"username": cand['user']['username'], "password": cand['user']['password']})
        if c_auth:
            c_token = c_auth['access']
            make_request("api/users/profile/setup/", cand['profile'], token=c_token, method="PUT")
            
            # Apply to random jobs
            if created_jobs:
                # Apply to 1 or 2 random jobs to create activity
                jobs_to_apply = random.sample(created_jobs, k=min(2, len(created_jobs)))
                for job_id in jobs_to_apply:
                    make_request("api/jobs/applications/", {"job": job_id}, token=c_token)
                    print(f"Candidate {cand['user']['username']} applied to job {job_id}")

    print("\n✅ Data population completely finished!")
    print("Your app should now be fully alive with real jobs and candidates!")

if __name__ == "__main__":
    main()
