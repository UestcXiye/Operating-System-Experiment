import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @BelongsProject:OS-process
 * @BelongsPackage:PACKAGE_NAME
 * @Author:Uestc_Xiye
 * @CreateTime:2020-12-01 16:52:29
 */
public class Pcb {
    /**
     * 变量说明
     * pcb: 进程控制块（Process Control Block）
     * readyQueue: 就绪队列
     * existProcess: 所有存活的进程，包括Running（运行状态）,Blocked（阻塞状态）,Ready（就绪状态）
     * currentProcess: 当前正在占用CPU的进程
     * pidGenerator: pid生成器，可以生成唯一的pid
     */
    private static final Pcb pcb=new Pcb();
    private static final Queue readyQueue=Queue.getreadyQueue();
    private static Map<String,Process> existProcess;
    private Process currentProcess;
    private AtomicInteger pidGenerator;

    private Pcb()
    {
        existProcess=new HashMap<>();
        pidGenerator=new AtomicInteger();
    }

    public Process createProcess(String processName,int priority)
    {
        Process currentProcess=pcb.getcurrentProcess();
        // 为新建进程分配pid，进程名，优先级，进程状态，资源，父进程和子进程等信息
        Process process=new Process(pcb.createpid(),processName,priority,Process.State.NEW,new ConcurrentHashMap<>(),currentProcess,new LinkedList<>());
        if(currentProcess!=null)
        {
            currentProcess.getchildren().add(process);
            process.setparent(currentProcess);
        }
        pcb.addexistProcess(process);
        readyQueue.addprocess(process);
        process.setstate(Process.State.READY);
        Pcb.scheduler();
        return process;
    }

    public static void scheduler()
    {
        Process currentProcess=pcb.getcurrentProcess();
        Process readyProcess=readyQueue.getprocess();
        if(readyProcess==null)
        {
            pcb.getcurrentProcess().setstate(Process.State.RUNNING);
            return;
        }
        else if(currentProcess==null)
        {
            readyQueue.deleteProcess(readyProcess);
            pcb.setcurrentProcess(readyProcess);
            readyProcess.setstate(Process.State.RUNNING);
            return;
        }
        else if(currentProcess.getstate()==Process.State.BLOCKED || currentProcess.getstate()==Process.State.TERMINATED)
        {
            readyQueue.deleteProcess(readyProcess);
            pcb.setcurrentProcess(readyProcess);
            readyProcess.setstate(Process.State.RUNNING);
        }
        else if(currentProcess.getstate()==Process.State.RUNNING)
        {
            if(currentProcess.getpriority()<readyProcess.getpriority())
            {
                preempt(readyProcess,currentProcess);
            }
        }
        else if(currentProcess.getstate()==Process.State.READY)
        {
            if(currentProcess.getpriority()<=readyProcess.getpriority())
            {
                preempt(readyProcess,currentProcess);
            }
            else
            {
                currentProcess.setstate(Process.State.RUNNING);
            }
        }
        return;
    }

    public static void preempt(Process readyProcess,Process currentProcess)
    {
        if(isExistName(currentProcess.getpName()))
        {
            readyQueue.addprocess(currentProcess);
            currentProcess.setstate(Process.State.READY);
            readyQueue.deleteProcess(readyProcess);
            pcb.setcurrentProcess(readyProcess);
            readyProcess.setstate(Process.State.RUNNING);
            return;
        }
    }

    public static void timeout()
    {
        pcb.getcurrentProcess().setstate(Process.State.READY);
        scheduler();
    }

    public void deleteexistProcess(Process process)
    {
        String name=process.getpName();
        existProcess.remove(name);
    }

    public void printProcessTree(Process process,int retract)
    {
        for (int i=0;i<retract;i++)
        {
            System.out.print("  ");
        }
        System.out.print("|-");
        printProcessDetail(process);
        List<Process> children=process.getchildren();
        for(Process child:children)
        {
            printProcessTree(child,retract+1);
        }
    }

    public void printProcessDetail(Process process)
    {
        System.out.print(process.getpName()+"(PID:"+process.getpid()+",进程状态："+process.getstate()+",优先级："+process.getpriority() + ",");
        if(process.getresourceMap().isEmpty())
        {
            System.out.println("(无资源占用))");
        }
        else
        {
            StringBuilder stringBuilder=new StringBuilder();
            stringBuilder.append("(");
            for(Map.Entry<Resource,Integer> entry:process.getresourceMap().entrySet())
            {
                Resource res=entry.getKey();
                int holdNum=entry.getValue();
                stringBuilder.append(",")
                             .append("R")
                             .append(res.getrid())
                             .append(":")
                             .append(holdNum);
            }
            stringBuilder.append(")");
            String result=stringBuilder.toString();
            System.out.println(result.replaceFirst(",",""));
        }
    }

    public void printExistProcess()
    {
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("existList:[");
        for(Map.Entry<String,Process> entry:existProcess.entrySet())
        {
            String name=entry.getKey();
            String state=entry.getValue().getstate().toString();
            stringBuilder.append(",")
                         .append(name)
                         .append("(")
                         .append(state)
                         .append(")");
        }
        stringBuilder.append("]");
        String result=stringBuilder.toString();
        System.out.println(result.replaceFirst(",", ""));
    }

    public int createpid()
    {
        return pidGenerator.getAndIncrement();
    }

    public void addexistProcess(Process process)
    {
        existProcess.put(process.getpName(),process);
    }

    public static boolean isExistName(String name)
    {
        return existProcess.containsKey(name);
    }

    public Process findProcess(String processName)
    {
        for(Map.Entry<String, Process> entry:existProcess.entrySet())
        {
            if(processName.equals(entry.getKey()))
            {
                return entry.getValue();
            }
        }
        return null;
    }

    public void deleteProcess(Process process)
    {
    }

    public void setcurrentProcess(Process currentProcess)
    {
        this.currentProcess=currentProcess;
    }

    public void setPidGenerator(AtomicInteger pidGenerator)
    {
        this.pidGenerator=pidGenerator;
    }

    public static Pcb getpcb()
    {
        return pcb;
    }

    public Queue getreadyQueue()
    {
        return readyQueue;
    }

    public Map<String,Process> getexistProcess()
    {
        return existProcess;
    }

    public Process getcurrentProcess()
    {
        return currentProcess;
    }

    public AtomicInteger getPidGenerator()
    {
        return pidGenerator;
    }
}
