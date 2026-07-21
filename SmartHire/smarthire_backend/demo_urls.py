from django.urls import path
from django.http import HttpResponse
from create_demo_data import create_demo_data

def populate_demo_data(request):
    try:
        create_demo_data()
        return HttpResponse('<h1>Success!</h1><p>Demo data populated successfully.</p>')
    except Exception as e:
        return HttpResponse(f'<h1>Error</h1><p>{str(e)}</p>')

urlpatterns = [
    path('api/populate-demo/', populate_demo_data),
]
