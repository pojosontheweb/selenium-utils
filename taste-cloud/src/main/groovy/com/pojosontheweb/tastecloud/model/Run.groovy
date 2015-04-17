package com.pojosontheweb.tastecloud.model

import com.pojosontheweb.selenium.Browsr

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.OrderBy
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Entity
@Table(name = 'taste_run')
class Run {

    @Id
    String id

    @NotNull
    Date queuedOn

    Date startedOn

    Date finishedOn

    @NotNull
    Browsr browsr = Browsr.Firefox

    @NotNull
    @Column(columnDefinition = 'text')
    String taste

    @NotNull
    String relativePath = 'tests.taste'

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    @OrderBy("logDate ASC")
    List<Log> logs

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    Suite suite

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    Test test

    @ManyToOne(fetch = FetchType.LAZY)
    RepositoryRun repositoryRun

    @ManyToOne(fetch = FetchType.LAZY)
    Taste fromTaste

    def getResult() {
        return suite ?: test
    }

    Log addLog(String text) {
        logs = logs ?: []
        Log l = new Log(logDate: new Date(), text: text)
        logs << l
        return l
    }

    RunSummary getSummary() {
        new RunSummary(
            browsr: browsr,
            queuedOn: queuedOn,
            startedOn: startedOn,
            finishedOn: finishedOn
        )
    }

    ResultSummary getResultSummary() {
        ResultSummary s = new ResultSummary(finished: finishedOn!=null)
        if (s.finished) {
            if (test) {
                test.success ? s.nbSuccess++ : s.nbFailed++
                s.elapsed = test.elapsed
            } else if (suite) {
                for (Test t : suite.testResults) {
                    t.success ? s.nbSuccess++ : s.nbFailed++
                }
                s.elapsed = suite.elapsed
            }
        }
        return s
    }

    Long getElapsed() {
        if (!startedOn) {
            return 0
        }
        def end = finishedOn ? finishedOn.time : System.currentTimeMillis()
        return end - startedOn.time
    }


}


@Entity
class Log {

    @Id @GeneratedValue
    Long id

    Date logDate

    @Column(columnDefinition = 'text')
    String text
}
