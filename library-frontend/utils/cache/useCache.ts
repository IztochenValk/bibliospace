type CacheEntry<T> = {
  data: T;
  timestamp: number;
};

const cache = new Map<string, CacheEntry<any>>();

export function useCache<T>(key: string, ttl = 30000) {
  const now = Date.now();

  const entry = cache.get(key);

  if (entry && now - entry.timestamp < ttl) {
    return {
      data: entry.data as T,
      cached: true,
    };
  }

  return {
    data: null,
    cached: false,
  };
}

export function setCache<T>(key: string, data: T) {
  cache.set(key, {
    data,
    timestamp: Date.now(),
  });
}
