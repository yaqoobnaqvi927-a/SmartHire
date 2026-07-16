"""
SmartHire Firebase Cloud Messaging (FCM) Service
Handles push notification delivery to Android (and optionally iOS) devices.

Requirements:
  pip install firebase-admin

Environment variables:
  FIREBASE_SERVICE_ACCOUNT_PATH — absolute path to the Firebase service account JSON file.
"""
import logging
import os

logger = logging.getLogger(__name__)

# Guard import so the app boots even without firebase-admin installed
try:
    import firebase_admin
    from firebase_admin import credentials, messaging
    FIREBASE_AVAILABLE = True
except ImportError:
    FIREBASE_AVAILABLE = False
    logger.warning(
        'firebase-admin package not installed. '
        'Push notifications are disabled. Run: pip install firebase-admin'
    )


# ---------------------------------------------------------------------------
# Internal initialisation (idempotent)
# ---------------------------------------------------------------------------

def _init_firebase() -> bool:
    """
    Initialise the Firebase Admin SDK exactly once per process.
    Returns True if the SDK is ready to use, False otherwise.
    """
    if not FIREBASE_AVAILABLE:
        return False

    # Already initialised — nothing to do
    if firebase_admin._apps:
        return True

    service_account_path = os.environ.get('FIREBASE_SERVICE_ACCOUNT_PATH', '')
    if not service_account_path:
        logger.warning(
            'FIREBASE_SERVICE_ACCOUNT_PATH environment variable is not set. '
            'Push notifications disabled.'
        )
        return False

    if not os.path.exists(service_account_path):
        logger.warning(
            f'Firebase service account file not found at: {service_account_path}. '
            'Push notifications disabled.'
        )
        return False

    try:
        cred = credentials.Certificate(service_account_path)
        firebase_admin.initialize_app(cred)
        logger.info('Firebase Admin SDK initialised successfully.')
        return True
    except Exception as exc:
        logger.error(f'Firebase Admin SDK initialisation failed: {exc}')
        return False


# ---------------------------------------------------------------------------
# Public API
# ---------------------------------------------------------------------------

def send_push_notification(
    user,
    title: str,
    body: str,
    data: dict = None,
) -> bool:
    """
    Send an FCM push notification to all registered devices of a user.

    Args:
        user:  A User model instance (the recipient).
        title: Notification title string.
        body:  Notification body text.
        data:  Optional dict of extra key-value pairs delivered to the app.
               All values are coerced to strings (FCM requirement).

    Returns:
        True if at least one message was delivered successfully, False otherwise.
    """
    if not _init_firebase():
        return False

    # Lazy import to avoid circular dependency at module load time
    from .models import DeviceToken

    tokens = list(
        DeviceToken.objects.filter(user=user).values_list('fcm_token', flat=True)
    )

    if not tokens:
        logger.info(f'No FCM tokens registered for user {user.id} ({user.username}).')
        return False

    # FCM requires all data values to be strings
    data_str = {str(k): str(v) for k, v in (data or {}).items()}

    messages_list = [
        messaging.Message(
            notification=messaging.Notification(title=title, body=body),
            data=data_str,
            token=token,
            android=messaging.AndroidConfig(
                priority='high',
                notification=messaging.AndroidNotification(
                    sound='default',
                    click_action='FLUTTER_NOTIFICATION_CLICK',
                ),
            ),
        )
        for token in tokens
    ]

    try:
        response = messaging.send_each(messages_list)

        # Remove any tokens that FCM reported as invalid/expired
        if response.failure_count > 0:
            for idx, send_response in enumerate(response.responses):
                if not send_response.success:
                    invalid_token = tokens[idx]
                    logger.warning(
                        f'Removing invalid FCM token (user {user.id}): '
                        f'{invalid_token[:20]}…'
                    )
                    DeviceToken.objects.filter(fcm_token=invalid_token).delete()

        logger.info(
            f'FCM push to user {user.id}: '
            f'{response.success_count} success, {response.failure_count} failure(s).'
        )
        return response.success_count > 0

    except Exception as exc:
        logger.error(f'FCM send_each() raised an exception: {exc}')
        return False


def send_push_to_topic(topic: str, title: str, body: str, data: dict = None) -> bool:
    """
    Broadcast a push notification to an FCM topic (e.g. 'new_jobs').
    Topics allow targeting large groups without iterating individual tokens.

    Args:
        topic: FCM topic name (no leading slash required).
        title: Notification title.
        body:  Notification body.
        data:  Optional string key-value pairs.

    Returns:
        True on success, False on failure.
    """
    if not _init_firebase():
        return False

    data_str = {str(k): str(v) for k, v in (data or {}).items()}

    message = messaging.Message(
        notification=messaging.Notification(title=title, body=body),
        data=data_str,
        topic=topic,
        android=messaging.AndroidConfig(
            priority='high',
            notification=messaging.AndroidNotification(sound='default'),
        ),
    )

    try:
        message_id = messaging.send(message)
        logger.info(f'FCM topic "{topic}" message sent. ID: {message_id}')
        return True
    except Exception as exc:
        logger.error(f'FCM topic send failed for topic "{topic}": {exc}')
        return False
