from django.urls import path
from django.http import HttpResponse

def populate_demo_data(request):
    try:
        from create_demo_data import create_demo_data
        from populate_quest_demo import populate_quest_data
        create_demo_data()
        populate_quest_data()
        return HttpResponse('<h1>Success!</h1><p>QUEST Nawabshah demo data populated successfully.</p>')
    except Exception as e:
        return HttpResponse(f'<h1>Error</h1><p>{str(e)}</p>')

urlpatterns = [
    path('api/populate-demo/', populate_demo_data),
]
