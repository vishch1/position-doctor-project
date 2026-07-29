import { axiosClient } from './axiosClient';
import { ApiResponse, CreatePositionRequest, PositionResponse, UpdatePositionRequest } from '../types';

export const positionApi = {
  getAll: async (): Promise<ApiResponse<PositionResponse[]>> => {
    const response = await axiosClient.get<ApiResponse<PositionResponse[]>>('/positions');
    return response.data;
  },

  getByPortfolioId: async (portfolioId: string): Promise<ApiResponse<PositionResponse[]>> => {
    const response = await axiosClient.get<ApiResponse<PositionResponse[]>>(`/positions/portfolio/${portfolioId}`);
    return response.data;
  },

  getById: async (id: string): Promise<ApiResponse<PositionResponse>> => {
    const response = await axiosClient.get<ApiResponse<PositionResponse>>(`/positions/${id}`);
    return response.data;
  },

  create: async (payload: CreatePositionRequest): Promise<ApiResponse<PositionResponse>> => {
    const response = await axiosClient.post<ApiResponse<PositionResponse>>('/positions', payload);
    return response.data;
  },

  update: async (id: string, payload: UpdatePositionRequest): Promise<ApiResponse<PositionResponse>> => {
    const response = await axiosClient.put<ApiResponse<PositionResponse>>(`/positions/${id}`, payload);
    return response.data;
  },

  delete: async (id: string): Promise<ApiResponse<void>> => {
    const response = await axiosClient.delete<ApiResponse<void>>(`/positions/${id}`);
    return response.data;
  },
};
