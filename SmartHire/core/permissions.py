"""
core/permissions.py
-------------------
Shared DRF permission classes used across the SmartHire API.

Role values map to the `role_type` field on the custom User model:
  - 'student'   → job-seeker / candidate
  - 'recruiter' → hiring-side user (company HR / hiring manager)
"""

from rest_framework.permissions import BasePermission


class IsCandidate(BasePermission):
    """Allow access only to authenticated users whose role_type is 'student'."""

    message = 'Access restricted to candidates only.'

    def has_permission(self, request, view):
        return (
            request.user
            and request.user.is_authenticated
            and request.user.role_type == 'student'
        )


class IsRecruiter(BasePermission):
    """Allow access only to authenticated users whose role_type is 'recruiter'."""

    message = 'Access restricted to recruiters only.'

    def has_permission(self, request, view):
        return (
            request.user
            and request.user.is_authenticated
            and request.user.role_type == 'recruiter'
        )


class IsOwnerOrRecruiter(BasePermission):
    """
    Object-level permission:
      - The candidate who owns the object can access it.
      - Any recruiter can also access it (e.g., to view applications).

    Supports two object shapes:
      1. obj.user        — direct user ownership (e.g. CandidateProfile)
      2. obj.candidate.user — nested ownership (e.g. Application → Candidate → User)
    """

    message = 'You do not have permission to access this resource.'

    def has_object_permission(self, request, view, obj):
        # Direct owner check (e.g. Profile, CV)
        if hasattr(obj, 'user'):
            return obj.user == request.user

        # Nested candidate ownership or recruiter access (e.g. Application)
        if hasattr(obj, 'candidate'):
            return (
                obj.candidate.user == request.user
                or request.user.role_type == 'recruiter'
            )

        return False
