import { axiosClient } from './axiosClient';
import { ApiResponse, DashboardResponse } from '../types';

export const dashboardApi = {
  getSummary: async (): Promise<ApiResponse<DashboardResponse>> => {
    const response = await axiosClient.get<ApiResponse<DashboardResponse>>('/dashboard');
    return response.data;
  },
};
