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
      navLinks: true,
      editable: true,
      selectable: true,
      selectMirror: true,
      dayMaxEvents: true,
      titleFormat: '',
      events: [
      ]
    };

    var calendar2 = new FullCalendar.Calendar(document.getElementById('calendar2'), calendarOptions);
    for(i = 0; i < lEventosJS.length; i++){
      calendar2.addEvent({
        title: lEventosJS[i].name,
        start: lEventosJS[i].date,
        end: lEventosJS[i].ending,
        url: '/events/' + lEventosJS[i].id
      });
    }
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