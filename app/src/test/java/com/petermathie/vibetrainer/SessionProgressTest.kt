package com.petermathie.vibetrainer

import com.petermathie.vibetrainer.domain.progress.SessionProgress
import com.petermathie.vibetrainer.ui.emptySet
import com.petermathie.vibetrainer.ui.parsePerformance
import org.junit.Test
import org.junit.Assert.*

class SessionProgressTest {
    @Test fun zeroAndWarmupsAreExcluded(){
        val set=emptySet("e",1).copy(weightKg=100.0,reps=0)
        assertNull(SessionProgress.score(set,80.0,0.0))
        assertNull(SessionProgress.score(set.copy(reps=5,setType="WARM_UP"),80.0,0.0))
    }
    @Test fun stacksReduceDifficultyWithinVariation(){
        val set=emptySet("e",1).copy(holdMillis=10000)
        assertTrue(SessionProgress.score(set,null,0.6)!! > SessionProgress.score(set,null,3.7)!!)
    }
    @Test fun compactWeightedEntryConvertsPounds(){
        val set=parsePerformance(emptySet("e",1),"220.46226218 x 5",false,true,true)!!
        assertEquals(100.0,set.weightKg!!,0.00001)
        assertEquals(5,set.reps)
        assertNull(parsePerformance(set,"100 x -2",false,true,false))
    }
}
