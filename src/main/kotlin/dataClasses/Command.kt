package dataClasses
data class Command(val command: String, val isAdminCommand: Boolean, val action: () -> Unit)