type ToastKind = "success" | "error" | "info" | "warning";

type ToastItem = {
  id: number;
  type: ToastKind;
  message: string;
};

export function useToast() {
  const items = useState<ToastItem[]>("toast:items", () => []);

  function remove(id: number): void {
    items.value = items.value.filter((item) => item.id !== id);
  }

  function push(type: ToastKind, message: string): void {
    const id = Date.now() + Math.floor(Math.random() * 1000);

    items.value.push({
      id,
      type,
      message,
    });

    if (import.meta.client) {
      const timeout = type === "error" ? 5000 : 3000;

      window.setTimeout(() => {
        remove(id);
      }, timeout);
    }

    if (import.meta.client) {
      const prefix =
        type === "success" ? "SUCCESS" :
        type === "error" ? "ERROR" :
        type === "warning" ? "WARNING" :
        "INFO";

      console.log(`[${prefix}] ${message}`);
    }
  }

  return {
    items,
    push,
    remove,
  };
}
