import json
from channels.generic.websocket import AsyncWebsocketConsumer
from channels.db import database_sync_to_async
from .models import ChatThread, ChatMessage
from django.contrib.auth import get_user_model

User = get_user_model()

class ChatConsumer(AsyncWebsocketConsumer):
    async def connect(self):
        self.thread_id = self.scope['url_route']['kwargs']['thread_id']
        self.room_group_name = f'chat_{self.thread_id}'

        await self.channel_layer.group_add(
            self.room_group_name,
            self.channel_name
        )
        await self.accept()

    async def disconnect(self, close_code):
        await self.channel_layer.group_discard(
            self.room_group_name,
            self.channel_name
        )

    async def receive(self, text_data):
        data = json.loads(text_data)
        content = data.get('content')
        sender_id = data.get('sender_id')

        if content and sender_id:
            saved_msg = await self.save_message(sender_id, self.thread_id, content)

            await self.channel_layer.group_send(
                self.room_group_name,
                {
                    'type': 'chat_message',
                    'content': content,
                    'sender_id': sender_id,
                    'sender_name': saved_msg['sender_name'],
                    'timestamp': saved_msg['timestamp'],
                    'message_id': saved_msg['id']
                }
            )

    async def chat_message(self, event):
        await self.send(text_data=json.dumps({
            'content': event['content'],
            'sender_id': event['sender_id'],
            'sender_name': event['sender_name'],
            'timestamp': event['timestamp'],
            'id': event['message_id']
        }))

    @database_sync_to_async
    def save_message(self, sender_id, thread_id, content):
        thread = ChatThread.objects.get(id=thread_id)
        sender = User.objects.get(id=sender_id)
        msg = ChatMessage.objects.create(thread=thread, sender=sender, content=content)
        return {
            'id': msg.id,
            'sender_name': sender.full_name or sender.username,
            'timestamp': msg.timestamp.isoformat()
        }
