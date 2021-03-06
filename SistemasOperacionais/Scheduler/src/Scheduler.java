import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author gabriel
 */
public class Scheduler extends Thread {
    List<Process> processes = new ArrayList<Process>();
    private Boolean running = true;
    private Integer quantum = 0;
    private Process runningProcess;
    private Integer nextPid = 0;
    private Integer currentTime = 0;
    private Integer totalTimeRow = 0;

    public void addProcess(Process p) {
        processes.add(p);
        updateCounter();
    }

    public Integer getCurrentTime() {
        return currentTime;
    }

    private void updateCounter() {
        SISOPInterface.labelProcessCount.setText("Processes Count: " + processes.size());
        SISOPInterface.labelCurrentTime.setText("Current Time: " + currentTime);
    }

    public void setQuantum(Integer quantum) {
        this.quantum = quantum;
    }

    public void stopSchedler() {
        running = false;
    }

    public Integer nextPid() {
        return nextPid++;
    }

    private void printIdleState() {
        SISOPInterface.outputTextArea.setText("IDLE!");
        if (nextPid > 0) {
            SISOPInterface.outputTextArea.append("\n");
            SISOPInterface.outputTextArea.append("TOTAL TIME IN ROW: " + totalTimeRow);
            SISOPInterface.outputTextArea.append("\n");
            SISOPInterface.outputTextArea.append("TOTAL OF PROCESSES: " + nextPid);
            SISOPInterface.outputTextArea.append("\n");
            SISOPInterface.outputTextArea.append("AVERAGE TIME IN ROW: " + getAverageTimeRow());
        }
    }

    private BigDecimal getAverageTimeRow() {
        return BigDecimal.valueOf(totalTimeRow).divide(BigDecimal.valueOf(nextPid), 2, RoundingMode.HALF_EVEN);
    }

    private void setRunningProcess(){
        processes.forEach(process -> {
                    if (runningProcess == null || process.getPriority() < runningProcess.getPriority())
                        runningProcess = process;
                }
        );
    }

    private void incrementSleepingTimeProcesses(){
        processes.stream()
                .filter(process -> !process.getPid().equals(runningProcess.getPid()))
                .forEach(Process::incrementSleepingTime);
    }

    private void finishProcess(){
        totalTimeRow += runningProcess.getSleppingTime();
        processes.remove(runningProcess);
        runningProcess = null;
    }

    @Override
    public void run() {
        while (running) {
            try {
                setRunningProcess();

                if (runningProcess == null) {
                    printIdleState();
                } else {
                    SISOPInterface.outputTextArea
                            .setText("RUNNING PROCESS PID = "
                                    + runningProcess.getPid());

                    SISOPInterface.outputTextArea.append("\n");

                    SISOPInterface.outputTextArea.append("INSERTION TIME = "
                            + runningProcess.getInsertionTime());

                    SISOPInterface.outputTextArea.append("\n");

                    SISOPInterface.outputTextArea.append("REMAINING TIME = "
                            + runningProcess.getRemainingTime());

                    runningProcess.runProcess();

                    incrementSleepingTimeProcesses();

                    if (runningProcess.isFinished()) {
                        finishProcess();
                    }
                }

                currentTime++;
                updateCounter();
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
