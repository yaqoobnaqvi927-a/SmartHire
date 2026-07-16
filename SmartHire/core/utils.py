"""
core/utils.py
-------------
Shared utility functions used across all SmartHire apps.
"""


def notify_user(
    user,
    title: str,
    message: str,
    notif_type: str = 'general',
    ref_id: int = None,
) -> None:
    """
    Create a persistent DB notification record for *user* and attempt to
    deliver a Firebase Cloud Messaging (FCM) push notification.

    The FCM push is intentionally fire-and-forget: a failure (missing FCM
    credentials, invalid token, network error, etc.) is logged but never
    raises so that the calling business logic is never blocked by a
    non-critical notification step.

    Args:
        user:       The User instance to notify.
        title:      Short notification headline (shown in OS notification tray).
        message:    Body text of the notification.
        notif_type: Category string stored on the record (e.g. 'interview',
                    'application', 'general').  Used by the mobile app to
                    route the user to the correct screen on tap.
        ref_id:     Optional integer primary key of the related object
                    (e.g. interview_id, application_id) for deep-linking.

    Returns:
        None — callers should not depend on the return value.
    """
    # 1. Persist the notification to the database so the in-app inbox
    #    always reflects the event even when FCM delivery fails.
    from communications.models import Notification  # local import avoids circular deps

    Notification.objects.create(
        user=user,
        title=title,
        message=message,
        notif_type=notif_type,
        ref_id=ref_id,
    )

    # 2. Best-effort FCM push — non-blocking on failure.
    try:
        from communications.fcm_service import send_push_notification  # noqa

        send_push_notification(
            user,
            title,
            message,
            data={
                'type': notif_type,
                'ref_id': str(ref_id) if ref_id is not None else '',
            },
        )
    except Exception as exc:  # pragma: no cover — FCM errors are environment-specific
        # Log but do not re-raise; push delivery is best-effort
        print(f'FCM push failed (non-critical): {exc}')
