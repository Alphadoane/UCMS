from django.contrib import admin
from .models import User, Role, UserRole, SystemSettings, Broadcast

# --- Site Branding ---
admin.site.site_header = "Alphadoane Student Portal Management"
admin.site.site_title = "Alphadoane Admin"
admin.site.index_title = "System Administration & Management"

# --- Identity & Access ---
@admin.register(User)
class UserAdmin(admin.ModelAdmin):
    list_display = ('email', 'first_name', 'last_name', 'is_staff', 'is_superuser', 'is_active')
    search_fields = ('email', 'first_name', 'last_name')
    list_filter = ('is_staff', 'is_superuser', 'is_active')
    ordering = ('email',)
    
    fieldsets = (
        ('Account Information', {
            'fields': ('email', 'password')
        }),
        ('Personal Info', {
            'fields': ('first_name', 'last_name')
        }),
        ('Permissions', {
            'fields': ('is_active', 'is_staff', 'is_superuser', 'groups', 'user_permissions')
        }),
        ('Important Dates', {
            'fields': ('last_login', 'created_at')
        }),
    )
    readonly_fields = ('created_at',)

@admin.register(Role)
class RoleAdmin(admin.ModelAdmin):
    list_display = ('name',)

@admin.register(UserRole)
class UserRoleAdmin(admin.ModelAdmin):
    list_display = ('user', 'role')
    list_filter = ('role',)
    search_fields = ('user__email',)

# --- System ---
@admin.register(SystemSettings)
class SystemSettingsAdmin(admin.ModelAdmin):
    list_display = ('student_email_domain',)
    
    def has_add_permission(self, request):
        # Usually only one setting obj should exist
        return not SystemSettings.objects.exists()

@admin.register(Broadcast)
class BroadcastAdmin(admin.ModelAdmin):
    list_display = ('title', 'target_audience', 'sender', 'created_at')
    list_filter = ('target_audience', 'created_at')
    search_fields = ('title', 'message')
    readonly_fields = ('created_at',)
