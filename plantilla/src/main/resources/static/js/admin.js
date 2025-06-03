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

    // Eventos: habilitar/deshabilitar con AJAX
    const eventButtons = document.querySelectorAll('.toggle-event');
    eventButtons.forEach(button => {
        button.addEventListener('click', async function(e) {
            e.preventDefault();
            const eventId = this.getAttribute('data-event-id');
            const isActive = this.classList.contains('btn-danger'); // Si es rojo, está activo

            const url = isActive
                ? `/events/${eventId}/disable`
                : `/events/${eventId}/enable`;

            try {
                const response = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'X-CSRF-TOKEN': csrfToken
                    }
                });

                if (!response.ok) {
                    throw new Error('Error al cambiar el estado del evento');
                }

                // Actualiza el estado visualmente
                const badge = this.closest('tr').querySelector('.badge');
                if (isActive) {
                    // Ahora está inactivo
                    this.classList.remove('btn-danger');
                    this.classList.add('btn-success');
                    this.innerHTML = '<i class="bi bi-check-circle"></i> Habilitar';
                    badge.classList.remove('bg-success');
                    badge.classList.add('bg-danger');
                    badge.textContent = 'Inactivo';
                } else {
                    // Ahora está activo
                    this.classList.remove('btn-success');
                    this.classList.add('btn-danger');
                    this.innerHTML = '<i class="bi bi-x-circle"></i> Deshabilitar';
                    badge.classList.remove('bg-danger');
                    badge.classList.add('bg-success');
                    badge.textContent = 'Activo';
                }
            } catch (error) {
                alert(error.message);
            }
        });
    });
});