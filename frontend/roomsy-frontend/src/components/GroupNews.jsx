import React, { useEffect, useState } from 'react';
import { groupApi } from '../api';

export default function GroupNews({ groupId }) {
  const [newsPage, setNewsPage] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!groupId) {
      setNewsPage(null);
      return;
    }
    let mounted = true;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const data = await groupApi.getGroupNews(groupId, { page: 0, size: 5, sortBy: 'createdAt', sortDirection: 'desc' });
        if (!mounted) return;
        setNewsPage(data);
      } catch (e) {
        if (!mounted) return;
        setError(e.message || 'Failed to load news');
      } finally {
        if (mounted) setLoading(false);
      }
    };
    load();
    return () => { mounted = false; };
  }, [groupId]);

  const items = newsPage?.content ?? newsPage?.items ?? [];

// helper para parsear un LocalDateTime ISO sin zona como fecha en zona local
const parseLocalDateTime = (s) => {
    if (!s) return null;
    const m = s.match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2})(\.\d+)?)?$/);
    if (!m) return new Date(s); // fallback
    const [, y, mo, d, h, mi, sec = '0', frac = ''] = m;
    const ms = frac ? Math.round(Number(frac) * 1000) : 0;
    return new Date(Number(y), Number(mo) - 1, Number(d), Number(h), Number(mi), Number(sec), ms);
};

const formatLocalDateTime = (s) => {
    const dt = parseLocalDateTime(s);
    if (!dt || isNaN(dt)) return s ?? '';
    return dt.toLocaleString(); // cambia opciones si quieres otro formato
};

const [selected, setSelected] = useState(null);

// cerrar modal con Escape
useEffect(() => {
    if (!selected) return;
    const onKey = (e) => {
        if (e.key === 'Escape') setSelected(null);
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
}, [selected]);

return (
    <>
        <div className="bg-white rounded-lg shadow-md p-4 w-80">
            <h3 className="text-lg font-semibold mb-4 text-gray-800">Group News</h3>

            {loading && <div className="text-sm text-gray-600">Loading news...</div>}
            {error && <div className="bg-red-50 border border-red-200 text-red-700 px-3 py-2 rounded text-sm">{error}</div>}

            {!loading && !error && items.length === 0 && (
                <div className="text-sm text-gray-600">No recent news</div>
            )}

            <ul className="space-y-3 mt-2">
                {items.map((n) => (
                    <li
                        key={n.id}
                        className="border rounded p-3 bg-gray-50 cursor-pointer hover:bg-gray-100 focus:ring-2 focus:ring-indigo-300"
                        role="button"
                        tabIndex={0}
                        onClick={() => setSelected(n)}
                        onKeyDown={(e) => {
                            if (e.key === 'Enter' || e.key === ' ') setSelected(n);
                        }}
                    >
                        <div className="text-sm font-medium text-gray-800">{n.name ?? 'Untitled'}</div>
                        <div className="text-xs text-gray-500 mt-2">{formatLocalDateTime(n.createdAt)}</div>
                    </li>
                ))}
            </ul>
        </div>

        {/* Modal */}
        {selected && (
            <div
                className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4"
                onClick={() => setSelected(null)}
                aria-modal="true"
                role="dialog"
            >
                <div
                    className="bg-white rounded-lg max-w-md w-full shadow-lg p-6"
                    onClick={(e) => e.stopPropagation()}
                >
                    <div className="flex justify-between items-start">
                        <h4 className="text-lg font-semibold text-gray-800">{selected.name ?? 'Untitled'}</h4>
                        <button
                            onClick={() => setSelected(null)}
                            className="text-gray-500 hover:text-gray-700 ml-4"
                            aria-label="Close"
                        >
                            ✕
                        </button>
                    </div>

                    <div className="mt-3 text-sm text-gray-600">
                        <div className="text-xs text-gray-500">Created:</div>
                        <div className="text-sm text-gray-700">{formatLocalDateTime(selected.createdAt)}</div>
                    </div>

                    {selected.content && (
                        <div className="mt-4 text-sm text-gray-800">
                            {selected.description}
                        </div>
                    )}

                    <div className="mt-6 flex justify-end">
                        <button
                            onClick={() => setSelected(null)}
                            className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
                        >
                            Close
                        </button>
                    </div>
                </div>
            </div>
        )}
    </>
);
}