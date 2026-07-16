"""
core/pagination.py
------------------
Standard paginator used across all SmartHire list endpoints.
Clients can override page size via ?page_size=N (capped at 100).
"""

from rest_framework.pagination import PageNumberPagination


class StandardPagination(PageNumberPagination):
    """
    Default page size: 20 results.
    Override per-request with ?page_size=<n> (max 100).
    """

    page_size = 20
    page_size_query_param = 'page_size'
    max_page_size = 100
