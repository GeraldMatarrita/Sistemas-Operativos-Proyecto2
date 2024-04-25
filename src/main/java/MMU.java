import java.util.*;

public class MMU {

    private static List<Page> virtualMemory;
    private static Page[] realMemory;
    private static final Integer MAX_RAM_KB = 6; // Max space for physical memory
    private static final Integer KB = 1000;
    private static int remainingRAM;
    private static Map<Integer, List<Integer>> symbolTable;

    private static int ptrCounter = 1;

    public MMU() {
        virtualMemory = new ArrayList<>();
        realMemory = new Page[MAX_RAM_KB];
        remainingRAM = MAX_RAM_KB;
        symbolTable = new HashMap<>();
    }

    public static List<Page> getVirtualMemory() {
        return virtualMemory;
    }

    public static void setVirtualMemory(List<Page> virtualMemory) {
        MMU.virtualMemory = virtualMemory;
    }

    public static Page[] getRealMemory() {
        return realMemory;
    }

    public static void setRealMemory(Page[] realMemory) {
        MMU.realMemory = realMemory;
    }

    public static Integer getRemainingRAM() {
        return remainingRAM;
    }

    public static void setRemainingRAM(Integer remainingRAM) {
        MMU.remainingRAM = remainingRAM;
    }

    public static Map<Integer, List<Integer>> getSymbolTable() {
        return symbolTable;
    }

    public static void setSymbolTable(Map<Integer, List<Integer>> symbolTable) {
        MMU.symbolTable = symbolTable;
    }

    /*
        * Create a new process in the memory
        * @param pid The process ID
        * @param size The size of the process in bytes
        * @return The pointer in the real memory where the process is stored
     */
    public Integer new_(Integer pid, Integer size) {

        int result = calculatePagesNeeded(size);

        // Check if the RAM is not full
        if (remainingRAM > 0) {
            // Create an array of pages to store them in the symbol table
            List<Integer> pages = new ArrayList<>();

            int remainingPages = result; // Number of pages to be stored in the RAM
            int ramIterator = 0; // Pointer to iterate over the RAM

            // While there is RAM available and there are pages to store
            while (remainingRAM != 0 && remainingPages > 0) {
                // Check if the current position in the RAM is empty
                if (realMemory[ramIterator] == null) {
                    // Create a new page and store it in the RAM
                    Page page = new Page(pid);
                    page.setInRealMemory(true);
                    page.setPhysicalAddress(ptrCounter);
                    realMemory[ramIterator] = page;
                    pages.add(page.getId());
                    remainingPages--;
                    remainingRAM--;
                }

                // Move the pointer to the next position in the RAM or reset it
                if (ramIterator == MAX_RAM_KB - 1) {
                    ramIterator = 0;
                } else {
                    ramIterator++;
                }
            }

            // If there are remaining pages, store them in the virtual memory
            if (remainingPages > 0) {
                for (int i = 0; i < remainingPages; i++) {
                    Page page = new Page(pid);
                    virtualMemory.add(page);
                    pages.add(page.getId());
                }
            }

            // Store the pages in the symbol table with the corresponding PID
            symbolTable.put(ptrCounter, pages);
            ptrCounter++;
        }

        return ptrCounter;
    }

    /*
        * Calculate the number of pages needed to store the process
        * @param size The size of the process in bytes
        * @return The number of pages needed to store the process
     */
    private static int calculatePagesNeeded(Integer size) {
        int result;
        // Check if the size is greater than 1KB
        if (size > KB) {
            // Calculate the number of pages needed
            result = size / KB;
            int residue = size % 1000;
            if (residue > 0) {
                result++;
            }
        } else {
            // If the size is less than 1KB, then only one page is needed
            result = 1;
        }
        return result;
    }

    public void use(Integer ptr) {

    }

    public void delete(Integer ptr) {
        if (symbolTable.containsKey(ptr)) {
            List<Integer> pages = symbolTable.get(ptr);
            for (Integer pageId : pages) {
                for (int i = 0; i < realMemory.length; i++) {
                    if (realMemory[i] != null && Objects.equals(realMemory[i].getId(), pageId)) {
                        realMemory[i] = null;
                        remainingRAM++;
                    }
                }
            }
            symbolTable.remove(ptr);
        }
    }

    public void kill(Integer pid) {

    }


}
