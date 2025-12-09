import React, { useEffect, useMemo, useState } from 'react';

export default function Calendar({
  events = [],
  onEventsChange = () => {},
  locale = undefined,
  onDayOpen = () => {},
  onEventClick = () => {}
}) {
  const [currentMonth, setCurrentMonth] = useState(() => {
    const d = new Date(); d.setDate(1); return d;
  });
  const [internalEvents, setInternalEvents] = useState([]);
  const [selectedDay, setSelectedDay] = useState(null); // 'YYYY-MM-DD'
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [showDayModal, setShowDayModal] = useState(false);

  useEffect(() => {
    setInternalEvents(Array.isArray(events) ? events.slice() : []);
  }, [events]);

  const toYMD = (d) => {
    const dt = new Date(d);
    const y = dt.getFullYear();
    const m = String(dt.getMonth() + 1).padStart(2, '0');
    const day = String(dt.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  };

  const parseLocalDateTime = (s) => {
    if (!s) return null;
    const m = String(s).match(/^(\d{4})-(\d{2})-(\d{2})T(.+)$/);
    if (m) return new Date(`${m[1]}-${m[2]}-${m[3]}T${m[4]}`);
    const d = new Date(s);
    return isNaN(d) ? null : d;
  };

  // map events by date key
  const eventsByDate = useMemo(() => {
    const map = {};
    for (const ev of internalEvents) {
      const raw = ev.date;
      const dt = parseLocalDateTime(raw) ?? new Date(raw);
      const key = toYMD(dt);
      map[key] = map[key] || [];
      map[key].push(ev);
    }
    return map;
  }, [internalEvents]);

  const calendar = useMemo(() => {
    const first = new Date(currentMonth);
    const year = first.getFullYear();
    const month = first.getMonth();
    const firstDayWeek = new Date(year, month, 1).getDay(); // 0..6 (Sun..Sat)
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const weeks = [];
    let week = new Array(firstDayWeek).fill(null);
    for (let d = 1; d <= daysInMonth; d++) {
      week.push(new Date(year, month, d));
      if (week.length === 7) { weeks.push(week); week = []; }
    }
    if (week.length) {
      while (week.length < 7) week.push(null);
      weeks.push(week);
    }
    return weeks;
  }, [currentMonth]);

  const prevMonth = () => { const d = new Date(currentMonth); d.setMonth(d.getMonth() - 1); setCurrentMonth(d); };
  const nextMonth = () => { const d = new Date(currentMonth); d.setMonth(d.getMonth() + 1); setCurrentMonth(d); };

  // open day modal
  const openDay = (ymd) => {
    setSelectedDay(ymd);
    setShowDayModal(true);
    onDayOpen?.(ymd);
  };

  useEffect(() => {
    if (!showDayModal) {
        setSelectedEvent(null);
    }
  }, [showDayModal]);

  return (
    <div className="bg-white rounded-lg shadow-md p-4">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <button onClick={prevMonth} className="px-3 py-1 bg-gray-100 rounded">{'<'}</button>
          <h3 className="text-lg font-semibold text-gray-800">{currentMonth.toLocaleString(locale, { month: 'long', year: 'numeric' })}</h3>
          <button onClick={nextMonth} className="px-3 py-1 bg-gray-100 rounded">{'>'}</button>
        </div>
      </div>

      <div className="grid grid-cols-7 gap-1 text-xs">
        {['Sun','Mon','Tue','Wed','Thu','Fri','Sat'].map(d => <div key={d} className="text-center font-medium py-1">{d}</div>)}
      </div>

      <div className="grid grid-cols-7 gap-1 mt-2">
        {calendar.map((week, wi) => (
          <React.Fragment key={wi}>
            {week.map((day, di) => {
              if (!day) return <div key={di} className="min-h-[80px] p-2 rounded bg-gray-50"></div>;
              const ymd = toYMD(day);
              const dayEvents = eventsByDate[ymd] || [];
              return (
                <div key={di} className="min-h-[80px] border p-2 bg-white rounded">
                  <div className="flex justify-between items-start">
                    <div
                      className="text-xs text-gray-800 cursor-pointer"
                      onClick={() => openDay(ymd)}
                    >
                      {day.getDate()}
                    </div>
                    <div className="text-xs text-gray-400">{dayEvents.length ? dayEvents.length : ''}</div>
                  </div>

                  <div className="mt-2 space-y-1">
                    {dayEvents.slice(0,3).map(ev => (
                      <div
                        key={ev.id}
                        className="text-xs p-1 rounded cursor-pointer hover:bg-gray-100 flex items-center gap-2"
                        onClick={() => { setSelectedEvent(ev); setSelectedDay(ymd); setShowDayModal(true); onEventClick?.(ev); }}
                      >
                        <span className={`truncate ${ev.completed ? 'text-gray-400' : 'text-gray-800'}`}>~ {ev.title ?? ev.name}</span>
                      </div>
                    ))}
                    {dayEvents.length > 3 && <div className="text-xs text-gray-400">+{dayEvents.length - 3} more</div>}
                  </div>
                </div>
              );
            })}
          </React.Fragment>
        ))}
      </div>
    </div>
  );
}