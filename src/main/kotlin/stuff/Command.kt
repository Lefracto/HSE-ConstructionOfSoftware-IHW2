package stuff
data class Command(val command: String, val isAdminCommand: Boolean, val action: () -> Unit)