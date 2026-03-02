from django.contrib import admin
from django.urls import path, include
from rest_framework_simplejwt.views import (
    TokenObtainPairView,
    TokenRefreshView,
)

urlpatterns = [
    path("admin/", admin.site.urls),
    # JWT endpoints
    path("api/auth/jwt/login", TokenObtainPairView.as_view(), name="token_obtain_pair"),
    path("api/auth/jwt/login/", TokenObtainPairView.as_view()),
    path("api/auth/jwt/refresh", TokenRefreshView.as_view(), name="token_refresh"),
    path("api/auth/jwt/refresh/", TokenRefreshView.as_view()),
    path("api/", include("student_api.core.urls")),
    path("api/admission/", include("admission.urls")),
    path("api/finance/", include("student_api.finance.urls")),
    path("api/library/", include("student_api.library.urls")),
]

from django.conf import settings
from django.conf.urls.static import static

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)



