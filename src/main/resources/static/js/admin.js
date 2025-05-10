document.addEventListener('DOMContentLoaded', function() {
    const toggleButtons = document.querySelectorAll('.toggle-user');
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    
    toggleButtons.forEach(button => {
        button.addEventListener('click', async function(e) {
            e.preventDefault();
            const userId = this.getAttribute('data-user-id');
            
            try {
                const response = await fetch(`/admin/toggle/${userId}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': csrfToken
                    }
                });

                const data = await response.json();
                
                if (!response.ok) {
                    throw new Error(data.error || 'Error al cambiar el estado');
                }

                // Update button state
                const isEnabled = data.enabled;
                button.textContent = isEnabled ? 'Inhabilitar' : 'Habilitar';
                button.className = `btn toggle-user ${isEnabled ? 'btn-danger' : 'btn-success'}`;

                // Update status badge
                const statusBadge = document.querySelector(`.status-badge[data-user-id="${userId}"]`);
                if (statusBadge) {
                    statusBadge.textContent = isEnabled ? 'Activo' : 'Inactivo';
                    statusBadge.className = `status-badge badge ${isEnabled ? 'bg-success' : 'bg-danger'}`;
                }

            } catch (error) {
                console.error('Error:', error);
                alert('Error al cambiar el estado del usuario: ' + error.message);
            }
        });
    });
});