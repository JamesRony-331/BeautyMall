<script setup>
import { session } from '../stores/session'
import { ArrowRight, TopRight } from '@element-plus/icons-vue'
</script>
<template>
  <div class="home-page">
    <section class="hero" aria-labelledby="hero-title">
      <img
        class="hero-image"
        src="/images/beauty-editorial.jpg"
        alt="柔和光影下的护肤瓶、粉饼与玫瑰色唇膏"
        fetchpriority="high"
        width="1536"
        height="1024"
      />
      <div class="hero-inner">
        <div class="hero-copy">
          <p class="hero-kicker">美妆灵感</p>
          <h1 id="hero-title">美，自有你的方式。</h1>
          <p class="hero-description">从了解肤质，到找到风格。<br />让适合自己的美，自然发生。</p>
          <router-link :to="session.token ? '/profile' : '/register'" class="button button-dark"
            >{{ session.token ? '进入个人中心' : '开启我的美妆之旅'
            }}<el-icon><ArrowRight /></el-icon></router-link
          ><a class="hero-secondary" href="#discover">探索更多灵感</a>
        </div>
      </div>
      <div class="hero-caption"><span>日常之美，始于适合。</span><span>美妆与风格探索</span></div>
    </section>
    <section id="discover" class="discovery section-wrap" aria-labelledby="discovery-title">
      <div class="section-heading">
        <div>
          <p class="section-label">关于你的美</p>
          <h2 id="discovery-title">不追随标准，发现适合。</h2>
        </div>
        <p>肤质、色彩、风格。<br />从你的真实需求出发，认识美妆的更多可能。</p>
      </div>
      <div class="editorial-grid">
        <article class="editorial-feature">
          <div class="editorial-image">
            <img
              src="/images/beauty-editorial.jpg"
              alt="磨砂护肤瓶与细腻粉饼的美妆静物"
              loading="lazy"
              width="1536"
              height="1024"
            /><span>护肤与彩妆</span>
          </div>
          <div class="editorial-caption">
            <div>
              <h3>了解产品，也了解自己。</h3>
              <p>从成分到肤质，让选购多一份依据。</p>
            </div>
            <span class="availability">即将开放</span>
          </div>
        </article>
        <article class="style-feature">
          <div class="style-art" aria-hidden="true">
            <div class="fabric-fold"></div>
            <span class="style-word">由你<br />定义。</span
            ><span class="style-note">色彩与风格的日常表达</span>
          </div>
          <div class="editorial-caption">
            <div>
              <h3>让妆容，呼应今天的穿搭。</h3>
              <p>从穿搭风格出发，寻找美妆搭配灵感。</p>
            </div>
            <span class="availability">即将开放</span>
          </div>
        </article>
      </div>
    </section>
    <section id="guide" class="guide-section">
      <div class="section-wrap guide-inner">
        <div>
          <p class="section-label">你的美妆助手</p>
          <h2>好的建议，<br />从倾听你开始。</h2>
        </div>
        <div class="guide-detail">
          <p>说说你的肤质、偏好与日常需求。<br />我们正在准备更懂你的智能导购体验。</p>
          <div class="guide-row">
            <span>对话式美妆推荐</span><span class="availability">即将开放</span>
          </div>
          <div class="guide-row">
            <span>穿搭风格识别与搭配</span><span class="availability">即将开放</span>
          </div>
          <router-link :to="session.token ? '/profile' : '/register'" class="text-link"
            >{{ session.token ? '管理我的账号' : '先创建我的账号' }}<el-icon><TopRight /></el-icon
          ></router-link>
        </div>
      </div>
    </section>
    <section class="join-section section-wrap">
      <p class="section-label">属于你的美妆空间</p>
      <h2>下一次发现，从这里开始。</h2>
      <p>创建账号，让美妆探索拥有自己的起点。</p>
      <router-link :to="session.token ? '/profile' : '/register'" class="button button-dark"
        >{{ session.token ? '我的个人中心' : '创建账号' }}<el-icon><ArrowRight /></el-icon
      ></router-link>
    </section>
  </div>
</template>
