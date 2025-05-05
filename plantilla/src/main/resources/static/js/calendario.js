document.addEventListener('DOMContentLoaded', function() {
    var calendarOptions = {
      initialDate: '2025-05-05',
      initialView: 'timeGridWeek',
      nowIndicator: true,
      headerToolbar: {
        left: 'prev, next, today',
        center: 'title',
        right: 'dayGridMonth,timeGridWeek,timeGridDay,listWeek'
      },
      navLinks: true, // can click day/week names to navigate views
      editable: true,
      selectable: true,
      selectMirror: true,
      dayMaxEvents: true, // allow "more" link when too many events
      events: [
      ]
    };

    var calendar2 = new FullCalendar.Calendar(document.getElementById('calendar2'), calendarOptions);
    calendar2.addEvent({
      title: 'Second Event',
      start: '2025-05-08T12:30:00'
    })
    calendar2.render();

    /*
    Modal
    */

    var modal = document.querySelector('.modal');
    var modalTrigger = document.querySelector('.modal-trigger');
    var modalOverlay = document.querySelector('.modal-overlay');

    modalTrigger.addEventListener('click', function() {
      modal.classList.add('is-visible');
      calendar2.updateSize();
    });
    modalOverlay.addEventListener('click', function() {
      modal.classList.remove('is-visible');
    });
  });