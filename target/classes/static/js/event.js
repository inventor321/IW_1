document.getElementById('messageForm').addEventListener('submit', function(e) {
    e.preventDefault();
    
    const messageInput = document.getElementById('messageInput');
    const eventId = document.getElementById('eventId').value;
    const message = messageInput.value.trim();
    
    if (!message) return;

    console.log('Enviando mensaje:', {eventId, message}); // Debug log

    fetch(`/event/${eventId}`, {  // URL actualizada
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
        },
        body: JSON.stringify({
            content: message
        })
    })
    .then(response => {
        console.log('Response status:', response.status);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('Mensaje enviado:', data);
        
        const chatMessages = document.getElementById('chatMessages');
        const messageDiv = document.createElement('div');
        messageDiv.className = 'message';
        messageDiv.innerHTML = `
            <strong>${data.from}</strong>
            <span>${data.text}</span>
            <small>${new Date(data.sent).toLocaleTimeString()}</small>
        `;
        chatMessages.appendChild(messageDiv);
        
        messageInput.value = '';
        chatMessages.scrollTop = chatMessages.scrollHeight;
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Error al enviar el mensaje');
    });
});