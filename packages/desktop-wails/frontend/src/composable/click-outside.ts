import { unref, Ref } from 'vue';
import { useEventListener } from './event';

export function useClickOutside(ref: Ref, cb: Function) {
  let target: any = null;

  const listener = (event: Event) => {
    if (!target) target = unref(ref);
    if (!(target === event.target || target.contains(event.target))) cb();
  };

  useEventListener(document.body, 'click', listener);
}