import {defineStore} from 'pinia'
import {ref} from 'vue'
import {getDictListByType} from '@/api/system'

export const useDictStore = defineStore('dict', () => {
  const dictCache = ref<Record<string, Record<string, string>[]>>({})

  async function loadDict(dictType: string): Promise<Record<string, string>[]> {
    if (dictCache.value[dictType]) return dictCache.value[dictType]
    try {
      const res = await getDictListByType({ dictType })
      dictCache.value[dictType] = res.list
      return res.list
    } catch {
      dictCache.value[dictType] = []
      return []
    }
  }

  function getLabel(dictType: string, value: string): string {
    const list = dictCache.value[dictType]
    if (!list) return value
    const item = list.find(d => d.dictValue === value)
    return item ? item.dictLabel : value
  }

  function setDictCache(dictType: string, list: Record<string, string>[]): void {
    dictCache.value[dictType] = list
  }

  return { dictCache, loadDict, getLabel, setDictCache }
})
