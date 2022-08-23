import { onMounted, onUnmounted } from 'vue';

export function useEventListener(target: any, event: any, callback: Function) {
  onMounted(() => target.addEventListener(event, callback));
  onUnmounted(() => target.removeEventListener(event, callback));
}