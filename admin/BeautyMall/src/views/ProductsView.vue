<script setup>
import StaticWorkspace from '../components/StaticWorkspace.vue'
import { products } from '../data/samples'
</script>
<template><StaticWorkspace kind="products" :rows="products" /></template>
