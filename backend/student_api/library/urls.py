from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import AdminBookViewSet, StudentBookListView

router = DefaultRouter()
router.register(r'admin/Library', AdminBookViewSet, basename='admin-library') # Note capital L in Library to match convention or url pattern requested? Standard is lowercase.
# Let's stick to standard practice: admin/library/books
router.register(r'admin/books', AdminBookViewSet, basename='admin-books')
router.register(r'student/books', StudentBookListView, basename='student-books')

urlpatterns = [
    path('', include(router.urls)),
]
