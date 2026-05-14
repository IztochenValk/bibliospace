<template>
  <canvas ref="canvasEl" class="pixel-rain-canvas" />
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from "vue";

const canvasEl = ref<HTMLCanvasElement | null>(null);

let ctx: CanvasRenderingContext2D | null = null;
let rafId: number | null = null;

let w = 0;
let h = 0;
let dpr = 1;

const COLORS = {
  cyan: "#22d3ee",
  blue: "#38bdf8",
  magenta: "#ff2bd6",
  red: "#ff5a5f",
  green: "#10b981",
  white: "rgba(255,255,255,0.95)",
};

const columns: Column[] = [];
const pixels: Pixel[] = [];
const sparks: Spark[] = [];

class Column {
  x = 0;
  y = 0;
  height = 0;
  speed = 0;
  width = 0;
  life = 0;
  lifeSpeed = 0;
  maxAlpha = 0;

  constructor() {
    this.reset();
  }

  reset() {
    this.x = Math.random() * w;
    this.y = Math.random() * -h;
    this.height = 140 + Math.random() * 520;

    // ralenti nettement
    this.speed = 0.18 + Math.random() * 0.65;

    this.width = Math.random() < 0.2 ? 10 + Math.random() * 16 : 2 + Math.random() * 2;

    this.life = 0;

    // cycle plus lent
    this.lifeSpeed = 0.0012 + Math.random() * 0.0015;

    this.maxAlpha = 0.14 + Math.random() * 0.28;
  }

  draw() {
    if (!ctx) return;

    this.life += this.lifeSpeed;

    let alpha = this.maxAlpha;

    if (this.life < 0.2) {
      alpha = (this.life / 0.2) * this.maxAlpha;
    } else if (this.life > 0.85) {
      alpha = ((1 - this.life) / 0.15) * this.maxAlpha;
    }

    const grad = ctx.createLinearGradient(0, this.y, 0, this.y + this.height);
    grad.addColorStop(0, "rgba(56,189,248,0)");
    grad.addColorStop(0.18, `rgba(34,211,238,${alpha * 0.7})`);
    grad.addColorStop(0.52, `rgba(56,189,248,${alpha})`);
    grad.addColorStop(0.82, `rgba(255,43,214,${alpha * 0.35})`);
    grad.addColorStop(1, "rgba(56,189,248,0)");

    ctx.fillStyle = grad;
    ctx.fillRect(this.x, this.y, this.width, this.height);

    this.y += this.speed;

    if (this.y > h + this.height || this.life >= 1) {
      this.reset();
    }
  }
}

class Pixel {
  x = 0;
  y = 0;
  size = 0;
  speed = 0;
  color = COLORS.cyan;

  constructor() {
    this.reset();
  }

  reset() {
    this.x = Math.random() * w;
    this.y = Math.random() * -h;
    this.size = 4 + Math.random() * 8;

    // ralenti
    this.speed = 0.2 + Math.random() * 0.8;

    const palette = [
      COLORS.cyan,
      COLORS.blue,
      COLORS.magenta,
      COLORS.red,
      COLORS.green,
    ];

    this.color = palette[Math.floor(Math.random() * palette.length)] ?? COLORS.cyan;
  }

  draw() {
    if (!ctx) return;

    ctx.shadowBlur = 8;
    ctx.shadowColor = this.color;
    ctx.fillStyle = this.color;
    ctx.fillRect(this.x, this.y, this.size, this.size);
    ctx.shadowBlur = 0;

    this.y += this.speed;

    if (this.y > h + 30) {
      this.reset();
    }
  }
}

class Spark {
  x = 0;
  y = 0;
  radius = 0;
  phase = 0;
  speed = 0;

  constructor() {
    this.reset();
  }

  reset() {
    this.x = Math.random() * w;
    this.y = Math.random() * h;
    this.radius = 1 + Math.random() * 2.4;
    this.phase = Math.random() * Math.PI * 2;

    // scintillement plus lent
    this.speed = 0.003 + Math.random() * 0.008;
  }

  draw() {
    if (!ctx) return;

    this.phase += this.speed;
    const a = 0.15 + (Math.sin(this.phase) * 0.5 + 0.5) * 0.35;

    ctx.fillStyle = `rgba(255,255,255,${a})`;
    ctx.beginPath();
    ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2);
    ctx.fill();
  }
}

function resize() {
  if (!canvasEl.value || !ctx) return;

  dpr = Math.max(1, window.devicePixelRatio || 1);
  w = window.innerWidth;
  h = window.innerHeight;

  canvasEl.value.width = Math.floor(w * dpr);
  canvasEl.value.height = Math.floor(h * dpr);
  canvasEl.value.style.width = `${w}px`;
  canvasEl.value.style.height = `${h}px`;

  ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
}

function animate() {
  if (!ctx) return;

  ctx.clearRect(0, 0, w, h);

  // voile un peu plus présent pour apaiser visuellement
  ctx.fillStyle = "rgba(8, 12, 24, 0.18)";
  ctx.fillRect(0, 0, w, h);

  for (const c of columns) c.draw();
  for (const p of pixels) p.draw();
  for (const s of sparks) s.draw();

  rafId = window.requestAnimationFrame(animate);
}

function initParticles() {
  columns.length = 0;
  pixels.length = 0;
  sparks.length = 0;

  // un peu moins d’éléments = moins d’agitation visuelle
  for (let i = 0; i < 95; i++) columns.push(new Column());
  for (let i = 0; i < 45; i++) pixels.push(new Pixel());
  for (let i = 0; i < 40; i++) sparks.push(new Spark());
}

onMounted(() => {
  const canvas = canvasEl.value;
  if (!canvas) return;

  ctx = canvas.getContext("2d");
  if (!ctx) return;

  resize();
  initParticles();

  window.addEventListener("resize", resize);
  animate();
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", resize);

  if (rafId !== null) {
    window.cancelAnimationFrame(rafId);
  }

  rafId = null;
  ctx = null;
});
</script>

<style scoped>
.pixel-rain-canvas {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  z-index: 1;
  pointer-events: none;
  display: block;
}
</style>