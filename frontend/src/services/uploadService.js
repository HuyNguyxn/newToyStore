import { apiClient } from './apiClient.js';

export function uploadImage(file, folder = 'general') {
  const formData = new FormData();
  formData.append('file', file);

  return apiClient(`/uploads/images?folder=${encodeURIComponent(folder)}`, {
    method: 'POST',
    body: formData,
  });
}

export function uploadVideo(file, folder = 'general') {
  const formData = new FormData();
  formData.append('file', file);

  return apiClient(`/uploads/videos?folder=${encodeURIComponent(folder)}`, {
    method: 'POST',
    body: formData,
  });
}
