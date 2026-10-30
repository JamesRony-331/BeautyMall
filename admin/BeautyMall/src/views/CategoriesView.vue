<script setup>
import StaticWorkspace from '../components/StaticWorkspace.vue'
import { categories } from '../data/samples'
</script>
<template><StaticWorkspace kind="categories" :rows="categories" /></template>
