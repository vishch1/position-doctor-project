import { axiosClient } from './axiosClient';
import { AlertResponse, ApiResponse } from '../types';

export const alertApi = {
  getAll: async (): Promise<ApiResponse<AlertResponse[]>> => {
    const response = await axiosClient.get<ApiResponse<AlertResponse[]>>('/alerts');
    return response.data;
  },

  getByUserId: async (userId: string): Promise<ApiResponse<AlertResponse[]>> => {
    const response = await axiosClient.get<ApiResponse<AlertResponse[]>>(`/alerts/${userId}`);
    return response.data;
  },

  markAsRead: async (alertId: string): Promise<ApiResponse<AlertResponse>> => {
    const response = await axiosClient.put<ApiResponse<AlertResponse>>(`/alerts/${alertId}/read`);
    return response.data;
  },
};
