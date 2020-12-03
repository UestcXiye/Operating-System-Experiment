import java.io.*;
import java.util.Scanner;

/**
 * @BelongsProject:OS-process
 * @BelongsPackage:PACKAGE_NAME
 * @Author:Uestc_Xiye
 * @CreateTime:2020-12-01 16:53:00
 */
public class main {
    private static final Pcb pcb=Pcb.getpcb();
    private static final Resource r1=new Resource(1,1);
    private static final Resource r2=new Resource(2,2);
    private static final Resource r3=new Resource(3,3);
    private static final Resource r4=new Resource(4,4);

    public static void main(String[] args) throws IOException
    {
        pcb.createProcess("init",0);
        System.out.print("init"+" ");
        if(args.length!=0)
        {
            loadFile(args[0]);
        }
        else
        {
            System.out.println();
            Scanner scanner=new Scanner(System.in);
            while(scanner.hasNextLine())
            {
                String input=scanner.nextLine();
                if(input.trim().equals(""))
                {
                    continue;
                }
                testShell(input);
            }
        }
    }

    public static void testShell(String input)
    {
        String[] commands=new String[]{input};
        for(String command:commands)
        {
            String[] cmds=command.split("\\s+");
            String options=cmds[0];
            switch(options)
            {
                case "cr":
                    if (cmds.length!=3)
                    {
                        System.out.println("Error! Please enter the legal parameters!");
                    }
                    else
                    {
                        String processName=cmds[1];
                        int priority=0;
                        try
                        {
                            priority=Integer.parseInt(cmds[2]);
                            if(priority<=0 || priority>2)
                            {
                                System.out.println("Error!Please enter the legal parameters!");
                                continue;
                            }
                        } catch (Exception e) {System.out.println("Error!Please enter the legal parameters!");}
                        if(pcb.isExistName(processName))
                        {
                            System.out.println("Error!Process "+processName+"already exists!Please select another process name!");
                            break;
                        }
                        pcb.createProcess(processName,priority);
                    }
                    break;
                case "de":
                    if(cmds.length!=2)
                    {
                        System.out.println("Error!Please enter the legal parameters!");
                    }
                    else
                    {
                        String processName=cmds[1];
                        Process process = pcb.findProcess(processName);
                        if(process==null)
                        {
                            System.out.println("Error!Process " + processName + "does not exist!");
                        }
                        else if(processName.equals("init"))
                        {
                            System.out.println("Error!You do not have permission to terminate the init process!");
                        }
                        else
                        {
                            process.destroy();
                        }
                    }
                    break;
                case "req":
                    if(cmds.length!=3)
                    {
                        System.out.println("Error!Please enter the legal parameters!");
                    }
                    else
                    {
                        String resourceName=cmds[1];
                        int need=0;
                        try
                        {
                            need=Integer.parseInt(cmds[2]);
                        } catch (Exception e) {System.out.println("Error!Please enter the legal parameters!");}
                        Process currentProcess=pcb.getcurrentProcess();
                        switch(resourceName)
                        {
                            case "R1":
                                r1.requestResource(currentProcess,need);
                                break;
                            case "R2":
                                r2.requestResource(currentProcess,need);
                                break;
                            case "R3":
                                r3.requestResource(currentProcess,need);
                                break;
                            case "R4":
                                r4.requestResource(currentProcess,need);
                                break;
                            default:
                                System.out.println("Error!Please enter the legal parameters!");
                        }
                    }
                    break;
                case "rel":
                    if(cmds.length!=3)
                    {
                        System.out.println("Error!Please enter the legal parameters!");
                    }
                    else
                    {
                        String resourceName=cmds[1];
                        int rel=0;
                        try
                        {
                            rel=Integer.parseInt(cmds[2]);
                        } catch (Exception e) {System.out.println("Error!Please enter the legal parameters!");}
                        Process currentProcess = pcb.getcurrentProcess();
                        switch(resourceName)
                        {
                            case "R1":
                                r1.releaseResource(currentProcess,rel);
                                break;
                            case "R2":
                                r2.releaseResource(currentProcess,rel);
                                break;
                            case "R3":
                                r3.releaseResource(currentProcess,rel);
                                break;
                            case "R4":
                                r4.releaseResource(currentProcess,rel);
                                break;
                            default:
                                System.out.println("Error!Please enter the legal parameters!");
                        }
                    }
                    break;
                case "to":
                    pcb.timeout();
                    break;
                case "lp":
                    if(cmds.length==1)
                    {
                        pcb.printProcessTree(pcb.findProcess("init"),0);
                    }
                    else if(cmds.length<3 || !cmds[1].equals("-p"))
                    {
                        System.out.println("Error!Please enter a legal parameter or command!");
                    }
                    else
                    {
                        String pName=cmds[2];
                        Process process=pcb.findProcess(pName);
                        if(process==null)
                        {
                            System.out.println("Error!Process "+pName+"does not exist!");
                        }
                        else
                        {
                            pcb.printProcessDetail(process);
                        }
                    }
                    break;
                case "lr":
                    r1.printStatus();
                    r2.printStatus();
                    r3.printStatus();
                    r4.printStatus();
                    break;
                case "exit":
                    System.out.println("Bye!");
                    System.exit(0);
                case "list":
                    pcb.printExistProcess();
                    break;
                default:
                    System.out.println("Error!Please enter the legal command!");
                    break;
            }
        }
        if(pcb.getcurrentProcess()!=null)
        {
            System.out.print(pcb.getcurrentProcess().getpName()+" ");
        }
    }

    private static void loadFile(String filePath) throws IOException
    {
        InputStream inputStream=new FileInputStream(filePath);
        LineNumberReader lineNumberReader=new LineNumberReader(new FileReader(filePath));
        String cmd=null;
        while((cmd=lineNumberReader.readLine())!=null)
        {
            if(!"".equals(cmd))
            {
                testShell(cmd);
            }
        }
    }

}