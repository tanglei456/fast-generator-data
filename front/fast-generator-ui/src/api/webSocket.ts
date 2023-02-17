import { defineStore } from "pinia";

export const webSocketStore = defineStore("webSocket", {
  state: () => ({
    //推送消息
    data: [],
  }),
  getters: {},

  actions: {
    addMsg(val: any) {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      this.data.push(val);
    },
  },
});

