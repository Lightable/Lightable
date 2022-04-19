<template>
  <div class="loading">
    <div class="main">
     <Logo class="loading-icon" height="128px" width="128px" :color="'var(--cyan)'" :spin="state != -1" />
      <div class="body">
        <span :state="state" class="notice" v-html="notice" />
      </div>
    </div>
    <transition name="bounce">
      <div class="timeout" v-if="timeout">
        <span class="rc"><b>Connection issues?</b></span
        ><br />
        <span class="details">Check your internet access</span>
      </div>
    </transition>
  </div>
</template>

<script lang="js">
import Logo from "./Icons/Logo.vue";
export default {
    name: "loading",
    props: {
        notice: {
            type: String,
            required: false,
            default: () => "Loading Chatty",
        },
        state: Number,
    },
    data() {
        return {
            timeout: false,
        };
    },
    mounted() {
        setTimeout(() => {
            this.timeout = true;
        }, 3000);
    },
    components: { Logo }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
.loading {
  height: 100vh;
  width: 100%;
  backdrop-filter: blur(10px);
  position: absolute;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  .main {
    height: 80%;
    display: flex;
    width: 100%;
    justify-content: center;
    align-items: center;
    flex-direction: column;
  }
}
.loading-icon {
  &[spin='true'] {
    animation: rotate 1s ease-in-out infinite;
  }
  &[spin='false'] {
    filter: brightness(50%);
  }
}
h3 {
  margin: 40px 0 0;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
.body {
  margin-top: 10%;
  span {
    color: var(--text);
    font-size: 24px;
    font-weight: bold;
  }
}
.timeout {
  .rc {
    color: var(--red);
  }
  .details {
    color: var(--blue);
  }
}
@keyframes rotate {
  0% {
    transform: rotate(0);
  }
  100% {
    transform: rotate(360deg);
  }
}
.notice {
  &[state='-1'] {
    color: var(--error);
  }
  &[state='0'] {
    color: var(--yellow);
  }
  &[state='1'] {
    color: var(--green);
  }
}
</style>
