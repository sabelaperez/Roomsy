import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../context/AuthContext';
import { groupApi } from '../api';
import GroupCreateJoin from './GroupCreateJoin';
import GroupNews from './GroupNews';

export default function Dashboard() {
  const { user, setUser } = useContext(AuthContext);
  const [groupInfo, setGroupInfo] = useState(null);
  const [loadingGroup, setLoadingGroup] = useState(false);

  useEffect(() => {
    if (user?.groupId) {
      load();
    } else {
      setGroupInfo(null);
    }
    async function load() {
      setLoadingGroup(true);
      try {
        const g = await groupApi.get(user.userId);
        setGroupInfo(g);
      } catch (_) {
        setGroupInfo(null);
      } finally { setLoadingGroup(false); }
    }
  }, [user?.groupId]);

  const onCreate = (data) => {
    setUser(prev => ({ ...prev, groupId: data.id }));
    setGroupInfo(data);
  };

  const onJoin = (data) => {
    setUser(prev => ({ ...prev, groupId: data.id }));
    setGroupInfo(data);
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-8 max-w-5xl mx-auto mt-4">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-gray-800">Dashboard</h2>
      </div>

      {user.groupId ? (
        <div className="flex w-full items-start gap-6">
          <div className="flex-1 w-1/2 space-y-6">
            {loadingGroup && (
                <div className="text-sm text-green-700">Loading group info...</div>
              )}

              {!groupInfo ? (
                <div className="text-sm text-green-700">No group info</div>
              ) : (
                <div className="flex flex-col md:flex-row items-start gap-6">
                  <div className="bg-white rounded-lg shadow-md p-4 w-full md:w-80">
                    <p className="text-sm text-gray-600">Welcome back!</p>
                    <p className="font-semibold text-gray-800">{user.fullName}</p>
                    <p className="text-sm text-gray-600">{user.email}</p>
                  </div>

                  <div className="bg-white rounded-lg shadow-md p-4 w-full md:w-80">
                    <p className="font-semibold text-gray-800 mb-2">Your Group</p>
                    <div>
                      <p className="text-sm text-gray-600">
                        Group: <span className="font-semibold text-gray-800">{groupInfo.name}</span>
                      </p>
                      <p className="text-sm text-gray-600">Members: {groupInfo.memberCount ?? '-'}</p>
                    </div>
                  </div>
                </div>
              )}
          </div>

          <div className="w-full md:w-1/3">
            <GroupNews groupId={user.groupId} />
          </div>
        </div>
      ) : (
        <div className="w-full space-y-6">
          <div className="bg-blue-50 border border-blue-200 p-4 rounded-md mb-6 w-full">
            <p className="text-sm text-gray-600">Welcome back!</p>
            <p className="font-semibold text-gray-800">{user.fullName}</p>
            <p className="text-sm text-gray-600">{user.email}</p>
          </div>
          <div className="w-full">
            <GroupCreateJoin user={user} onCreate={onCreate} onJoin={onJoin} />
          </div>
        </div>
      )}
    </div>
  );
}