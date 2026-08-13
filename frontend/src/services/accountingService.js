import { apiClient } from './apiClient.js';

function query(params = {}) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value);
  });
  const value = search.toString();
  return value ? `?${value}` : '';
}

export const getAccountingDashboard = (params) => apiClient(`/api/accounting/dashboard${query(params)}`);
export const getLedgerAccounts = (params) => apiClient(`/api/accounting/accounts${query(params)}`);
export const getJournalEntries = (params) => apiClient(`/api/accounting/journal-entries${query(params)}`);
export const getJournalEntry = (id) => apiClient(`/api/accounting/journal-entries/${id}`);
export const getGeneralLedger = (accountCode, params) => apiClient(`/api/accounting/general-ledger/${accountCode}${query(params)}`);
export const getTrialBalance = (params) => apiClient(`/api/accounting/reports/trial-balance${query(params)}`);
export const getIncomeStatement = (params) => apiClient(`/api/accounting/reports/income-statement${query(params)}`);
export const getAccountingReconciliationPreview = () => apiClient('/api/accounting/reconciliation/preview');
export const executeAccountingReconciliation = () => apiClient('/api/accounting/reconciliation/execute', {
  method: 'POST',
});

export const createManualJournalEntry = (payload) => apiClient('/api/accounting/journal-entries', {
  method: 'POST',
  body: JSON.stringify(payload),
});

export const reverseJournalEntry = (id) => apiClient(`/api/accounting/journal-entries/${id}/reverse`, {
  method: 'POST',
});
