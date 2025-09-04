export const StartRules: readonly string[];

export class SyntaxError extends Error {
  name: string;
  message: string;
  expected: any[];
  found: any;
  location: {
    start: { offset: number; line: number; column: number };
    end: { offset: number; line: number; column: number };
  };
  constructor(message?: string, expected?: any[], found?: any, location?: any);
}

export interface EqFilter {
  type: 'eq';
  values: string[];
  field: string;
  negated: boolean;
}

export interface InFilter {
  type: 'in';
  fields: string[];
  value: string;
}

export interface TermFilter {
  type: 'term';
  value: string;
}

export type Filter = EqFilter | InFilter | TermFilter;

export function parse(input: string, options?: {
  startRule?: string;
}): Filter[];

