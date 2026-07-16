import os
import re

directory = r"e:\FYP\SmartHire"

# Ordered by length descending to prevent partial replacements
replacements = [
    (r'\bStudentProfileSerializer\b', 'CandidateProfileSerializer'),
    (r'\bStudentProfile\b', 'CandidateProfile'),
    (r'\bstudent_profile\b', 'candidate_profile'),
    (r'\bJobApplicationSerializer\b', 'ApplicationSerializer'),
    (r'\bJobApplication\b', 'Application'),
    (r'\bJobSerializer\b', 'JobPostingSerializer'),
    (r'\bJobViewSet\b', 'JobPostingViewSet'),
    (r'\bJob\b(?!\w)', 'JobPosting'), # JobPosting but not Jobs or Application
    (r'\bInterviewSerializer\b', 'ScheduledInterviewSerializer'),
    (r'\bInterview\b', 'ScheduledInterview'),
]

for root, dirs, files in os.walk(directory):
    if 'migrations' in root or '__pycache__' in root or '.venv' in root:
        continue
    for file in files:
        if file.endswith('.py'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            new_content = content
            for old, new in replacements:
                new_content = re.sub(old, new, new_content)
                
            if new_content != content:
                print(f"Refactored: {filepath}")
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(new_content)

print("Done refactoring model names.")
