import { axiosClient } from './axiosClient';
import { ApiResponse, PositionHealthReport, RecommendationResponse } from '../types';

export const diagnosisApi = {
  getDiagnosis: async (positionId: string): Promise<ApiResponse<PositionHealthReport>> => {
    const response = await axiosClient.get<ApiResponse<PositionHealthReport>>(`/diagnosis/${positionId}`);
    return response.data;
  },

  getRecommendation: async (positionId: string): Promise<ApiResponse<RecommendationResponse>> => {
    const response = await axiosClient.get<ApiResponse<RecommendationResponse>>(`/recommendation/${positionId}`);
    return response.data;
  },
};
