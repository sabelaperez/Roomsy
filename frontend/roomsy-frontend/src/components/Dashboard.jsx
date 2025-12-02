import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../context/AuthContext';
import { groupApi } from '../api';
import GroupCreateJoin from './GroupCreateJoin';
import GroupInfo from './GroupInfo';
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
    <div className="bg-white rounded-lg shadow-md p-8 max-w-5xl mx-auto mt-10">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-gray-800">Dashboard</h2>
      </div>

      {user.groupId ? (
        <div className="flex items-start gap-6">
          <div className="flex-1">
            <div className="bg-blue-50 border border-blue-200 p-4 rounded-md mb-6">
              <p className="text-sm text-gray-600">Welcome back!</p>
              <p className="font-semibold text-gray-800">{user.fullName}</p>
              <p className="text-sm text-gray-600">{user.email}</p>
            </div>

            <GroupInfo
              groupInfo={groupInfo}
              loading={loadingGroup}
              onUpdate={setGroupInfo}
            />
          </div>
          <div className="w-80">
            <GroupNews groupId={user.groupId} />
          </div>
        </div>
      ) : (
        <div>
          <div className="bg-blue-50 border border-blue-200 p-4 rounded-md mb-6">
            <p className="text-sm text-gray-600">Welcome back!</p>
            <p className="font-semibold text-gray-800">{user.fullName}</p>
            <p className="text-sm text-gray-600">{user.email}</p>
          </div>
          <GroupCreateJoin user={user} onCreate={onCreate} onJoin={onJoin} />
        </div>
      )}
    </div>
  );
}