package x.Entt.UIAPI.Utils;

import java.util.List;

public class Pagination<T> {

    private final List<T> items;
    private final int itemsPerPage;
    private int page;

    public Pagination(List<T> items, int itemsPerPage) {
        if (itemsPerPage <= 0) {
            throw new IllegalArgumentException("Items per page must be greater than 0.");
        }

        this.items = List.copyOf(items);
        this.itemsPerPage = itemsPerPage;
    }

    public List<T> getCurrentItems() {
        int start = page * itemsPerPage;

        if (start >= items.size()) {
            return List.of();
        }

        int end = Math.min(start + itemsPerPage, items.size());

        return items.subList(start, end);
    }

    public boolean next() {
        if (!hasNext()) {
            return false;
        }

        page++;
        return true;
    }

    public boolean previous() {
        if (!hasPrevious()) {
            return false;
        }

        page--;
        return true;
    }

    public boolean hasNext() {
        return page + 1 < getPageCount();
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public int getPage() {
        return page;
    }

    public int getPageCount() {
        return Math.max(1, (int) Math.ceil((double) items.size() / itemsPerPage));
    }
}