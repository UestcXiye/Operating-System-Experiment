import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

/**
 * @BelongsProject:OS-process
 * @BelongsPackage:PACKAGE_NAME
 * @Author:Uestc_Xiye
 * @CreateTime:2020-12-01 16:51:48
 */
public class Resource {
    /**
     * 变量说明
     * rid: 资源的id，唯一
     * maxResource: 资源的最大数量
     * remainingResource: 剩余的资源的数量
     * blockedDeque: 在一个资源上阻塞的进程队列
     */
    private int rid;
    private int maxResource;
    private int remainingResource;
    private Deque<BlockedProcess> blockedDeque;
    private static final Pcb pcb=Pcb.getpcb();
    private static final Queue readyQueue=Queue.getreadyQueue();

    public class BlockedProcess
    {
        /**
         * 变量说明
         * process: 进程
         * need: 需要请求的资源数量
         */
        private Process process;
        private int need;

        public BlockedProcess(Process process,int need)
        {
            this.process=process;
            this.need=need;
        }

        public void setprocess(Process process)
        {
            this.process=process;
        }

        public void setneed(int need)
        {
            this.need=need;
        }

        public Process getprocess()
        {
            return process;
        }

        public int getneed()
        {
            return need;
        }
    }

    public Resource(int rid,int maxResource)
    {
        this.rid=rid;
        this.maxResource=maxResource;
        this.remainingResource=maxResource;
        blockedDeque=new LinkedList<>();
    }

    public void addremainingResource(int num)
    {
        this.remainingResource+=num;
    }

    public void deleteblockedProcess(Process process)
    {
        for(BlockedProcess blockedProcess:blockedDeque)
        {
            if(blockedProcess.getprocess()==process)
            {
                blockedDeque.remove(blockedProcess);
            }
        }
    }

    public void requestResource(Process process,int need)
    {
        // 若请求数量大于最大数量,则请求失败
        if(need>maxResource)
        {
            System.out.println("Request Resource Failed!");
            return;
        }
        // 对于非init进程,将该进程加入阻塞队列，并设置进程为阻塞状态
        else if(need>remainingResource && !"init".equals(process.getpName()))
        {
            blockedDeque.addLast(new BlockedProcess(process,need));
            process.setstate(Process.State.BLOCKED);
            process.setblockedResource(this);
            Pcb.scheduler();
            return;
        }
        // 对于init进程,不阻塞
        else if(need>remainingResource && "init".equals(process.getpName()))
        {
            return;
        }
        // 若可以正常分配资源，则剩余资源的数量减少，已分配资源的数量增加
        else
        {
            remainingResource-=need;
            Map<Resource,Integer> resourceMap=process.getresourceMap();
            if(resourceMap.containsKey(this))
            {
                Integer alreadyNum=resourceMap.get(this);
                resourceMap.put(this,alreadyNum+need);
            }
            else
            {
                resourceMap.put(this,need);
            }
        }
    }

    public void releaseResource(Process process)
    {
        int num=0;
        num=process.getresourceMap().remove(this);
        if(num==0)
        {
            return;
        }
        remainingResource+=num;
        while(!blockedDeque.isEmpty())
        {
            BlockedProcess blockedProcess = blockedDeque.peekFirst();
            int need=blockedProcess.getneed();
            // 若剩余资源数量大于need，则唤醒阻塞队列队头的一个进程
            if(remainingResource>= need)
            {
                Process readyProcess=blockedProcess.getprocess();
                requestResource(readyProcess,need);
                blockedDeque.removeFirst();
                readyQueue.addprocess(readyProcess);
                readyProcess.setstate(Process.State.READY);
                readyProcess.setblockedResource(null);
                // 若唤醒的进程优先级高于当前进程优先级,则抢占执行
                if(readyProcess.getpriority()>pcb.getcurrentProcess().getpriority())
                {
                    pcb.preempt(readyProcess,pcb.getcurrentProcess());
                }
            }
            else
            {
                break;
            }
        }
    }

    public void releaseResource(Process process,int num)
    {
        if(num==0)
        {
            return;
        }
        remainingResource+=num;
        while(!blockedDeque.isEmpty())
        {
            BlockedProcess blockedProcess = blockedDeque.peekFirst();
            int need=blockedProcess.getneed();
            // 若剩余资源数量大于need，则唤醒阻塞队列队头的一个进程
            if(remainingResource>= need)
            { 
                Process readyProcess=blockedProcess.getprocess();
                requestResource(readyProcess,need);
                blockedDeque.removeFirst();
                readyQueue.addprocess(readyProcess);
                readyProcess.setstate(Process.State.READY);
                readyProcess.setblockedResource(null);
                // 若唤醒的进程优先级高于当前进程优先级,则抢占执行
                if(readyProcess.getpriority()>pcb.getcurrentProcess().getpriority())
                { 
                    pcb.preempt(readyProcess,pcb.getcurrentProcess());
                }
            }
            else
            {
                break;
            }
        }
    }

    public void printStatus()
    {
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("resource-").append(rid)
                     .append("{maxResource=").append(maxResource)
                     .append(",remainingResource:").append(remainingResource)
                     .append(",")
                     .append("blockedDeque[");
        for(BlockedProcess bProcess:blockedDeque)
        {
            stringBuilder.append(",{")
                         .append(bProcess.getprocess().getpName())
                         .append(":")
                         .append(bProcess.getneed())
                         .append("}");
        }
        stringBuilder.append("]}");
        String result=stringBuilder.toString();
        System.out.println(result.replace("[,", "["));
    }

    public void setrid(int rid)
    {
        this.rid=rid;
    }

    public void setmaxResource(int maxResource)
    {
        this.maxResource=maxResource;
    }

    public void setremainingResource(int remainingResource)
    {
        this.remainingResource=remainingResource;
    }

    public void setblockedDeque(Deque<BlockedProcess> blockedDeque)
    {
        this.blockedDeque=blockedDeque;
    }

    public int getrid()
    {
        return rid;
    }

    public int getmaxResource()
    {
        return maxResource;
    }

    public int getremainingResource()
    {
        return remainingResource;
    }

    public Deque<BlockedProcess> getblockedDeque()
    {
        return blockedDeque;
    }

}
