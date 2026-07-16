from rest_framework import viewsets, permissions, status
from rest_framework.response import Response
from rest_framework.decorators import action
from .models import ChatMessage, Notification, ChatThread
from .serializers import ChatMessageSerializer, NotificationSerializer, ChatThreadSerializer
from jobs.models import Application

class CommunicationViewSet(viewsets.ViewSet):
    permission_classes = [permissions.IsAuthenticated]

    @action(detail=False, methods=['get'])
    def application_chat(self, request):
        app_id = request.query_params.get('application_id')
        if not app_id:
            return Response({"error": "application_id is required"}, status=status.HTTP_400_BAD_REQUEST)
            
        try:
            application = Application.objects.get(id=app_id)
        except Application.DoesNotExist:
            return Response(status=status.HTTP_404_NOT_FOUND)
            
        # Get or create Thread
        thread, _ = ChatThread.objects.get_or_create(
            job=application.job,
            candidate=application.candidate,
            recruiter=application.job.recruiter
        )
            
        messages = ChatMessage.objects.filter(thread=thread)
        serializer = ChatMessageSerializer(messages, many=True)
        return Response({
            "thread_id": thread.id,
            "messages": serializer.data
        })

    @action(detail=False, methods=['post'])
    def send_message(self, request):
        thread_id = request.data.get('thread_id')
        content = request.data.get('content')
        
        if not thread_id or not content:
            return Response({"error": "thread_id and content required"}, status=status.HTTP_400_BAD_REQUEST)
            
        try:
            thread = ChatThread.objects.get(id=thread_id)
        except ChatThread.DoesNotExist:
            return Response(status=status.HTTP_404_NOT_FOUND)
            
        msg = ChatMessage.objects.create(
            thread=thread,
            sender=request.user,
            content=content
        )
        serializer = ChatMessageSerializer(msg)
        return Response(serializer.data, status=status.HTTP_201_CREATED)

    @action(detail=False, methods=['get'])
    def notifications(self, request):
        notifs = Notification.objects.filter(user=request.user)
        serializer = NotificationSerializer(notifs, many=True)
        return Response(serializer.data)

    @action(detail=True, methods=['patch'])
    def mark_read(self, request, pk=None):
        try:
            notif = Notification.objects.get(id=pk, user=request.user)
            notif.is_read = True
            notif.save()
            return Response({"status": "read"})
        except Notification.DoesNotExist:
            return Response(status=status.HTTP_404_NOT_FOUND)
